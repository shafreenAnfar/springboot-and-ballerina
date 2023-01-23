import ballerina/time;
import ballerina/http;
import ballerina/constraint;

service class ResponseErrorInterceptor {
    *http:ResponseErrorInterceptor;

    remote function interceptResponseError(error err) returns UserNotFound|UserBadRequest|UserInternalServerError {
        ErrorDetails errorDetails = {
            timeStamp: time:utcNow(), 
            message: err.message(), 
            details: ""
        };
        
        if err is UserNotFoundError {
            UserNotFound userNotFound = {
                body: errorDetails
            };
            return userNotFound;
        } else if err is constraint:Error {
            UserBadRequest userBadRequest = {
                body: errorDetails
            };
            return userBadRequest;
        } else {
            UserInternalServerError userInternalServerError = {
                body: errorDetails
            };
            return userInternalServerError;
        }
    }
}

type UserNotFound record {|
    *http:NotFound;
    ErrorDetails body;
|};

type UserBadRequest record {|
    *http:NotFound;
    ErrorDetails body;
|};

type UserInternalServerError record {|
    *http:InternalServerError;
    ErrorDetails body;
|};