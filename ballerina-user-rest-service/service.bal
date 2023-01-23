import ballerinax/java.jdbc;
import ballerina/http;
import ballerina/sql;
import ballerinax/mysql.driver as _;

listener http:Listener userListener = new (9090,
    interceptors = [new ResponseErrorInterceptor()]
);

service /dept on userListener {

    final jdbc:Client userDb;

    public function init() returns error? {
        self.userDb = check new("jdbc:h2:file:~/testdb", "sa", ());
    }

    resource function get users() returns User[]|error {
        stream<User, sql:Error?> userStream = self.userDb->query(`SELECT * FROM USER_DETAILS`);
        return from User user in userStream
            select user;
    }

    resource function get users/[int id]() returns User|http:NotFound|error {
        User|error result = self.userDb->queryRow(`SELECT * FROM USER_DETAILS WHERE ID = ${id}`);
        if result is sql:NoRowsError {
            return http:NOT_FOUND;
        } else {
            return result;
        }
    }

    resource function post users(@http:Payload NewUser newUser) returns http:Created|error {
        _ = check self.userDb->execute(`
            INSERT INTO USER_DETAILS(BIRTH_DATE, NAME)
            VALUES (${newUser.birthDate}, ${newUser.name});`);
        return http:CREATED;
    }

    resource function get users/[int id]/posts() returns Post[]|UserNotFoundError|error {
        User|error result = self.userDb->queryRow(`SELECT * FROM USER_DETAILS WHERE ID = ${id}`);
        if result is sql:NoRowsError {
            return error UserNotFoundError("id: " + id.toString());
        }

        stream<Post, sql:Error?> postStream = self.userDb->query(`SELECT ID, DESCRIPTION FROM POST WHERE USER_ID = ${id}`);
        Post[]|error posts = from Post post in postStream
            select post;
        return posts;
    }

    resource function post users/[int id]/posts(@http:Payload NewPost newPost) returns http:Created|UserNotFoundError|error {
        User|error result = self.userDb->queryRow(`SELECT * FROM USER_DETAILS WHERE ID = ${id}`);
        if result is sql:NoRowsError {
            return error UserNotFoundError("id: " + id.toString());
        }

        _ = check self.userDb->execute(`
            INSERT INTO POST(DESCRIPTION, USER_ID)
            VALUES (${newPost.description}, ${id});`);
        return http:CREATED;
    }
}
