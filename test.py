#!/usr/bin/env python

from pymemcache.client.hash import HashClient
client = HashClient([
             ('127.0.0.1', 9999)
             ])
client.set('some_key', 'some value')
result = client.get('some_key')
print(result)
client.delete('some_key')
print(client.get('some_key'))
