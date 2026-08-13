#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { TravelConnectStack } from '../lib/travelconnect-stack';

const app = new cdk.App();
new TravelConnectStack(app, 'TravelConnectStack', {
  env: {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: process.env.CDK_DEFAULT_REGION ?? 'eu-west-1',
  },
  description: 'TravelConnect serverless audit infrastructure',
});
