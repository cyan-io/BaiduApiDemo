import requests
import json
API_KEY = '0klD8NaKj4wLCGE4HfXyhd7l'
SECRECT_KEY = 'qdVeenFNQd9iAlHGaYfD29iGsEQiBekr'

host = f'https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id={API_KEY}&client_secret={SECRECT_KEY}'
response = requests.get(host)
if response:
    print(response.json().get('access_token'))