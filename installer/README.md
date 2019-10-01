# Exactly installers

Currently only **Fedora 30 64-bit RPM** installer build is supported.

## Installer build

Run the `build.sh` command. This requires docker installed on your machine. The build process should run entirely in a [fedora:30](https://hub.docker.com/_/fedora) derived docker container
and after it ends an RPM file should appear in the same directory.

## Installer test

After you've built the installer, you can check it with the `test.sh` command. It will install the RPM in a fresh [fedora:30](https://hub.docker.com/_/fedora) derived docker container and check if it works properly
