/**
 * TravelConnect Booking Audit Lambda
 *
 * Triggered by the Notification Service when a booking completes.
 * Persists an immutable audit record to DynamoDB.
 *
 * DynamoDB record structure:
 * - bookingId (PK): UUID of the booking
 * - eventTimestamp (SK): ISO timestamp
 * - travelerId: UUID
 * - bookingReference: e.g. "TC-ABC12345"
 * - eventType: "BOOKING_COMPLETED"
 * - totalAmount: decimal string
 * - currency: "GBP"
 * - supplierSummary: JSON string of supplier details
 * - traceId: for distributed tracing
 * - ttl: Unix timestamp 90 days in future (auto-deletion)
 */

const { DynamoDBClient, PutItemCommand } = require('@aws-sdk/client-dynamodb');
const { marshall } = require('@aws-sdk/util-dynamodb');

const client = new DynamoDBClient({ region: process.env.REGION ?? 'eu-west-1' });
const TABLE_NAME = process.env.TABLE_NAME ?? 'travelconnect-booking-audit';

exports.handler = async (event) => {
    console.log('Received BookingCompleted event:', JSON.stringify(event, null, 2));

    const {
        bookingId,
        travelerId,
        bookingReference,
        totalAmount,
        currency,
        traceId,
        completedAt,
        supplierSummary
    } = event;

    const now = new Date();
    const ttlSeconds = Math.floor(now.getTime() / 1000) + (90 * 24 * 60 * 60); // 90 days

    const auditRecord = {
        bookingId: bookingId,
        eventTimestamp: completedAt ?? now.toISOString(),
        travelerId: travelerId,
        bookingReference: bookingReference ?? 'UNKNOWN',
        eventType: 'BOOKING_COMPLETED',
        totalAmount: totalAmount ? totalAmount.toString() : '0',
        currency: currency ?? 'GBP',
        supplierSummary: supplierSummary ?? '',
        traceId: traceId ?? '',
        processedAt: now.toISOString(),
        ttl: ttlSeconds
    };

    try {
        const command = new PutItemCommand({
            TableName: TABLE_NAME,
            Item: marshall(auditRecord),
            // Idempotency: don't overwrite if already exists
            ConditionExpression: 'attribute_not_exists(bookingId)'
        });

        await client.send(command);

        console.log(`Audit record saved: bookingId=${bookingId}, ref=${bookingReference}`);
        return {
            statusCode: 200,
            body: { message: 'Audit record saved', bookingId }
        };
    } catch (error) {
        if (error.name === 'ConditionalCheckFailedException') {
            console.log(`Audit record already exists for bookingId=${bookingId} — idempotent skip`);
            return { statusCode: 200, body: { message: 'Already processed', bookingId } };
        }
        console.error('Error saving audit record:', error);
        throw error;
    }
};
