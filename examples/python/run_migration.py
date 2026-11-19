import sys
import time

import requests

API_URL = "https://api.flexify.io"
API_TOKEN = "<your API token>"  # replace with your API token
HEADERS = {"Authorization": f"Bearer {API_TOKEN}"}

# Replace with your storage account IDs (see list_storage_accounts.py) and bucket names
SOURCE_STORAGE_ACCOUNT_ID = 1
SOURCE_BUCKET = "source-bucket"
DESTINATION_STORAGE_ACCOUNT_ID = 2
DESTINATION_BUCKET = "destination-bucket"

migration = {
    "settings": {
        "name": "API example",
        "migrationMode": "COPY",  # or MOVE
        "conflictResolution": "NEWER",  # or SOURCE, DESTINATION
        "existingDataInDestination": "KEEP",  # or CLEAN
        "comparisonMethod": None,  # or AUTO, PROBE_ONLY, LIST_ONLY, LIST_PROBE
        "skipIfHashMatches": True,
        "strictConsistencyCheck": False,
        "verifyBuckets": True,
        "dryRun": False,

        # Engines
        "enginesSelectionMethod": "AUTO",  # or SHARED, EXISTING, DEPLOYMENT        
        "enginesLocation": None, # or {"cloudCode": "AWS", "region": "us-east-1", "netConfig": "flexify-direct"}
        "maxEngines": None,  # or a number
        "maxStreams": None,  # or a number
        "slotsPerMapping": None,  # or a number

        # Which objects to migrate
        "objectKeyFilter": None,  # or a pattern such as "photos/*"
        "lastModifiedFrom": None,  # or a date such as "2026-01-01T00:00:00Z"
        "lastModifiedTo": None,  # or a date such as "2026-12-31T23:59:59Z"

        # Limits
        "maxBytesPerSecond": None,  # or a number
        "maxRequestsPerSecond": None,  # or a number

        # Multipart upload
        "multipartLimit": None,  # or the minimum object size in bytes
        "multipartPartSize": None,  # or the part size in bytes
        "multipartConcurrency": None,  # or the number of parts uploaded in parallel

        # Retries
        "maxRetries": 100,
        "maxRetriesForCopy": 10,
        "retryTimeout": 5,  # seconds
        "maxRetryTimeout": 60,  # seconds

        # Objects in an archival tier
        "autoRestoreIfArchived": False,
        "restoreTier": None,  # or BULK, STANDARD, EXPEDITED
        "restoreDays": None,  # or the number of days to keep the restored objects
        "restoreMaxSize": None,  # or the maximum total size in bytes

        # Other
        "removeHeaders": None,  # or a list of header names
        "uploadTimestampMode": None,  # for Backblaze B2: ACTUAL, ORIGINAL or CUSTOM
        "uploadTimestampValue": None,  # for CUSTOM: a date such as "2026-01-01T00:00:00Z"
        "migrationReportLevel": None,  # or ALL, NOT_SKIPPED, FAILED, NONE
    },
    "mappings": [
        {
            "sourceStorageAccountId": SOURCE_STORAGE_ACCOUNT_ID,
            "sourceBucketName": SOURCE_BUCKET,
            "destStorageAccountId": DESTINATION_STORAGE_ACCOUNT_ID,
            "destBucketName": DESTINATION_BUCKET,
            "destBucketNewRegion": None,  # or the region to create the destination bucket in
            "keyRemovePrefix": None,  # or a prefix to remove from each object key
            "keyAddPrefix": None,  # or a prefix to add to each object key
            "objectsListUri": None,  # or the URI of a text file with the objects to migrate
            "cdnUrl": None,  # or the base URL of a CDN to read the source objects through
        }
    ],
}

response = requests.post(f"{API_URL}/migrations", headers=HEADERS, json=migration, timeout=60)
response.raise_for_status()
migration_id = response.json()["id"]
print("Started migration", migration_id)

while True:
    response = requests.get(f"{API_URL}/migrations/{migration_id}", headers=HEADERS, timeout=60)
    response.raise_for_status()
    stat = response.json()["stat"]
    state = stat["state"]
    print(state, f'{stat.get("progress") or 0:.0%}')
    if state in ("SUCCEEDED", "FAILED", "STOPPED"):
        break
    time.sleep(5)

if state != "SUCCEEDED":
    sys.exit(1)
