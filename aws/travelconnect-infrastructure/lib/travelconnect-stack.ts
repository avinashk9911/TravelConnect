import * as cdk from 'aws-cdk-lib';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as dynamodb from 'aws-cdk-lib/aws-dynamodb';
import * as logs from 'aws-cdk-lib/aws-logs';
import { Construct } from 'constructs';

export class TravelConnectStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // ── DynamoDB Audit Table ──────────────────────────────────────────────
    // NoSQL — ideal here because audit records are write-once, read-by-key.
    // DynamoDB's pay-per-request billing means zero cost when idle.
    const auditTable = new dynamodb.Table(this, 'BookingAuditTable', {
      tableName: 'travelconnect-booking-audit',
      partitionKey: {
        name: 'bookingId',
        type: dynamodb.AttributeType.STRING,
      },
      sortKey: {
        name: 'eventTimestamp',
        type: dynamodb.AttributeType.STRING,
      },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      // Point-in-time recovery for compliance
      pointInTimeRecovery: true,
      // Auto-delete old records after 90 days (TTL)
      timeToLiveAttribute: 'ttl',
      removalPolicy: cdk.RemovalPolicy.DESTROY, // use RETAIN in production
    });

    // GSI — query by travelerId to find all their bookings
    auditTable.addGlobalSecondaryIndex({
      indexName: 'travelerId-index',
      partitionKey: {
        name: 'travelerId',
        type: dynamodb.AttributeType.STRING,
      },
      sortKey: {
        name: 'eventTimestamp',
        type: dynamodb.AttributeType.STRING,
      },
      projectionType: dynamodb.ProjectionType.ALL,
    });

    // ── Lambda Function ────────────────────────────────────────────────────
    const auditLambda = new lambda.Function(this, 'BookingAuditLambda', {
      functionName: 'travelconnect-booking-audit',
      runtime: lambda.Runtime.NODEJS_20_X,
      handler: 'index.handler',
      code: lambda.Code.fromAsset('../lambda/booking-audit'),
      environment: {
        TABLE_NAME: auditTable.tableName,
        REGION: this.region,
      },
      timeout: cdk.Duration.seconds(30),
      memorySize: 256,
      logRetention: logs.RetentionDays.ONE_MONTH,
      description: 'Persists BookingCompleted events to DynamoDB for audit',
    });

    // Grant Lambda permission to write to DynamoDB
    auditTable.grantWriteData(auditLambda);

    // ── Outputs ─────────────────────────────────────────────────────────────
    new cdk.CfnOutput(this, 'AuditTableName', {
      value: auditTable.tableName,
      description: 'DynamoDB audit table name',
    });

    new cdk.CfnOutput(this, 'AuditLambdaArn', {
      value: auditLambda.functionArn,
      description: 'Booking audit Lambda ARN',
    });

    new cdk.CfnOutput(this, 'AuditLambdaName', {
      value: auditLambda.functionName,
      description: 'Booking audit Lambda function name',
    });
  }
}
