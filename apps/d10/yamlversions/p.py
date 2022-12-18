import yaml

print("ATT")
with open('att.yaml', 'r') as f:
   dv = yaml.safe_load(f)
   print(dv)


print("EVA")
with open('eva.yaml', 'r') as f:
   dv = yaml.safe_load(f)
   print(dv)