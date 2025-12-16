#!/bin/bash
# Script to create keystore for signing the app
# This will create a keystore file for release signing

echo "Creating keystore for Construction Calculators app..."
echo ""

# Set keystore parameters
KEYSTORE_NAME="construction-release.keystore"
KEY_ALIAS="construction"
KEYSTORE_PASSWORD="construction123"
KEY_PASSWORD="construction123"
VALIDITY=10000

echo "Keystore name: $KEYSTORE_NAME"
echo "Key alias: $KEY_ALIAS"
echo "Validity: $VALIDITY days"
echo ""

# Create keystore
keytool -genkey -v -keystore "$KEYSTORE_NAME" -alias "$KEY_ALIAS" -keyalg RSA -keysize 2048 -validity "$VALIDITY" -storepass "$KEYSTORE_PASSWORD" -keypass "$KEY_PASSWORD" -dname "CN=Calc1, OU=Development, O=Calc1, L=Moscow, ST=Moscow, C=RU"

if [ $? -eq 0 ]; then
    echo ""
    echo "Keystore created successfully!"
    echo ""
    echo "IMPORTANT: Change the default passwords in keystore.properties file!"
    echo "Store the keystore file and passwords securely!"
else
    echo ""
    echo "Error creating keystore!"
    exit 1
fi



