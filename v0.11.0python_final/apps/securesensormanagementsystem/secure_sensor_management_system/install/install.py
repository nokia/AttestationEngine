import json
import socket
import os
import shutil
from pathlib import Path


def create_device_config():
    # Structure of the device_config.json
    device_config = {
        "itemid": "CHANGE THIS TO A CORRECT ONE",
        "hostname": socket.gethostname(),
        "address": get_ip_address(),
    }
    data_folder = Path("/etc/iotDevice/")
    file_name = "device_config.json"
    save_to_device(data_folder, file_name, device_config)


def create_client_config():
    # Structure of the client_config.json
    client_config = {
        "mqtt_host": "192.168.0.24",
        "mqtt_port": 1883,
        "mqtt_keepalive": 60
    }
    data_folder = Path("/etc/iotDevice/")
    file_name = "mqtt_client_config.json"
    save_to_device(data_folder, file_name, client_config)


def copy_service_files():
    # Service files copied under systemd
    source = f"{os.path.dirname(os.path.abspath(__file__))}/service_files/"
    destination = "/etc/systemd/system/"
    files = os.listdir(source)

    for f in files:
        shutil.copy(source+f, destination)
    print("install.py: Files copied")


def get_ip_address():
    ip_address = ''
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    ip_address = s.getsockname()[0]
    s.close()
    return ip_address


def save_to_device(data_path, write_to_file, file_to_be_written):
    json_object = json.dumps(file_to_be_written, indent=4)
    if os.path.isdir(data_path) == False:
        os.mkdir(data_path)
        print("install.py: Directory created")
        file_to_write = data_path / write_to_file
        try:
            with open(file_to_write, "w") as outfile:
                outfile.write(json_object)
                print("install.py: Config created: ", write_to_file)
        except IOError:
            print("install.py: Congfi creation erro: ", write_to_file)
    else:
        print("Directory already exists")
        file_to_write = data_path / write_to_file
        try:
            with open(file_to_write, "w") as outfile:
                outfile.write(json_object)
                print("install.py: Config created: ", write_to_file)
        except IOError:
            print("install.py: Congfi creation erro: ", write_to_file)

if __name__ == "__main__":
    copy_service_files()
    create_device_config()
    create_client_config()