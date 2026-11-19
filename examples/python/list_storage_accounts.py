import requests

API_URL = "https://api.flexify.io"
API_TOKEN = "<your API token>"  # replace with your API token

response = requests.get(
    f"{API_URL}/storage-accounts",
    params={"include-buckets": "false"},
    headers={"Authorization": f"Bearer {API_TOKEN}"},
    timeout=60,
)
response.raise_for_status()

for account in response.json():
    print(account["id"], account["settings"]["name"])
