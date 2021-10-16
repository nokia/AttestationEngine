# Installation

IGNORE THIS README - IT IS OUT OF DATE - USE THE ONE IN THE ROOT DIRECTORY

`ta.service` need to be placed in /etc/systemd/system and made executable by all

Edit ta.service, ta.start and ta.stop to ensure that the correct files are being pointed to. I will write a proper isntaller one day but at the moment as we're targtting Ubuntu, Raspbian and Deity (Blessed be His noodly appendages) knows what then you'll just have to customise these each time

```bash
chmod 644 ta.service
```

To enable

```bash
sudo systemd enable ta.service
```


Then `ta.service` needs to be updated so that the paths to the ta.py file are correct and the `Wants` and `After` directives are correctly set for machines that are (or are not) running the tpm2_abrmd resource management daemon.
