import ballerina/time;

type UserNotFoundError distinct error;

type NegativeSentimentError distinct error;

type ErrorDetails record {|
    time:Utc timeStamp;
    string message;
    string details;
|};