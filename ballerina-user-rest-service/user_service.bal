import ballerinax/java.jdbc;
import ballerina/http;
import ballerina/sql;
import ballerinax/mysql.driver as _;

listener http:Listener userListener = new (9090,
    interceptors = [new ResponseErrorInterceptor()]
);

service /medium on userListener {

    final jdbc:Client userDb;

    public function init() returns error? {
        self.userDb = check new("jdbc:h2:file:./testdb", "sa", ());
    }
    
    # Get all the users
    # 
    # + return - The list of users or error message
    resource function get users() returns User[]|error {
        stream<User, sql:Error?> userStream = self.userDb->query(`SELECT * FROM USER_DETAILS`);
        return from User user in userStream
            select user;
    }

    # Get a specific user
    # 
    # + id - The user ID of the user to be retrived
    # + return - A specific user or error message
    resource function get users/[int id]() returns User|error {
        User|error result = self.userDb->queryRow(`SELECT * FROM USER_DETAILS WHERE ID = ${id}`);
        if result is sql:NoRowsError {
            return error UserNotFoundError("id: " + id.toString());
        } else {
            return result;
        }
    }

    # Create a new user
    # 
    # + newUser - The user details of the new user
    # + return - The created message or error message
    resource function post users(@http:Payload NewUser newUser) returns http:Created|error {
        _ = check self.userDb->execute(`
            INSERT INTO USER_DETAILS(BIRTH_DATE, NAME)
            VALUES (${newUser.birthDate}, ${newUser.name});`);
        return http:CREATED;
    }

    # Delete a user
    # 
    # + id - The user ID of the user to be deleted
    # + return - The success message or error message
    resource function delete users/[int id]() returns http:NoContent|error {
        _ = check self.userDb->execute(`
            DELETE FROM USER_DETAILS WHERE ID = ${id};`);
        return http:NO_CONTENT;
    }

    # Get posts for a give user
    # 
    # + id - The user ID for which posts are retrieved
    # + return - A list of posts or error message
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

    # Create a post for a given user
    # 
    # + id - The user ID for which the post is created
    # + return - The created message or error message
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
