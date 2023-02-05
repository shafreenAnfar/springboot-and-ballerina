import ballerina/http;
import ballerina/sql;
import ballerina/mime;
import ballerinax/mysql.driver as _;
import ballerinax/mysql;

configurable boolean moderate = ?;

listener http:Listener userListener = new (9090,
    interceptors = [new ResponseErrorInterceptor()]
);

service /social\-media on userListener {

    final mysql:Client userDb;
    final http:Client sentimentEndpoint;

    public function init() returns error? {
        self.userDb = check new (host = "localhost", port = 3306, user = "root", password = "dummypassword");
        self.sentimentEndpoint = check new("localhost:8088", 
            retryConfig = {
                interval: 3
            }
        );
    }
    
    # Get all the users
    # 
    # + return - The list of users or error message
    resource function get users() returns User[]|error {
        stream<User, sql:Error?> userStream = self.userDb->query(`SELECT * FROM social_media_database.user_details`);
        return from User user in userStream
            select user;
    }

    # Get a specific user
    # 
    # + id - The user ID of the user to be retrived
    # + return - A specific user or error message
    resource function get users/[int id]() returns User|error {
        User|error result = self.userDb->queryRow(`SELECT * FROM social_media_database.user_details WHERE ID = ${id}`);
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
            INSERT INTO social_media_database.user_details(birth_date, name)
            VALUES (${newUser.birthDate}, ${newUser.name});`);
        return http:CREATED;
    }

    # Delete a user
    # 
    # + id - The user ID of the user to be deleted
    # + return - The success message or error message
    resource function delete users/[int id]() returns http:NoContent|error {
        _ = check self.userDb->execute(`
            DELETE FROM social_media_database.user_details WHERE id = ${id};`);
        return http:NO_CONTENT;
    }

    # Get posts for a give user
    # 
    # + id - The user ID for which posts are retrieved
    # + return - A list of posts or error message
    resource function get users/[int id]/posts() returns Post[]|error {
        User|error result = self.userDb->queryRow(`SELECT * FROM social_media_database.user_details WHERE id = ${id}`);
        if result is sql:NoRowsError {
            return error UserNotFoundError("id: " + id.toString());
        }

        stream<Post, sql:Error?> postStream = self.userDb->query(`SELECT id, description FROM social_media_database.post WHERE user_id = ${id}`);
        Post[]|error posts = from Post post in postStream
            select post;
        return posts;
    }

    # Create a post for a given user
    # 
    # + id - The user ID for which the post is created
    # + return - The created message or error message
    resource function post users/[int id]/posts(@http:Payload NewPost newPost) returns http:Created|error {
        User|error result = self.userDb->queryRow(`SELECT * FROM social_media_database.user_details WHERE id = ${id}`);
        if result is sql:NoRowsError {
            return error UserNotFoundError("id: " + id.toString());
        }

        Sentiment sentiment = check self.sentimentEndpoint->/text\-processing/api/sentiment.post(
            { text: newPost },
            mediatype = mime:APPLICATION_FORM_URLENCODED
        );
        if sentiment.label == "neg" {
            return error NegativeSentimentError("Negative sentiment detected");
        }

        _ = check self.userDb->execute(`
            INSERT INTO social_media_database.post(description, user_id)
            VALUES (${newPost.description}, ${id});`);
        return http:CREATED;
    }
}
