import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

public class ListDynamoTable {
    public static void main(String[] args) {
        // AWS Credentials (can be obtained from environment or IAM role)
       String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");

        // Specify the region where the tables should be listed
        Region region = Region.US_WEST_2;  // Change to the region you're working with

        // Create DynamoDbClient
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(region)  // Specify the region for the client
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        try {
            // Create the ListTables request
            ListTablesRequest listTablesRequest = ListTablesRequest.builder()
                    //.exclusiveStartTableName("") // Optional: can be used for pagination
                    .build();

            // List all tables
            ListTablesResponse listTablesResponse = dynamoDbClient.listTables(listTablesRequest);
            System.out.println("Tables in region " + region.id() + ":");

            // Print the names of all the tables
            listTablesResponse.tableNames().forEach(tableName -> {
                System.out.println("- " + tableName);
            });

        } catch (Exception e) {
            System.err.println("Error occurred while listing tables: " + e.getMessage());
        } finally {
            // Close the DynamoDbClient
            dynamoDbClient.close();
        }
    }
}
