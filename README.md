# NoticeMe

[![Java CI](https://github.com/megoRU/NoticeMe/actions/workflows/ci_cd.yml/badge.svg)](https://github.com/megoRU/HangmanDiscordBot/actions/workflows/ci_cd.yml)
[![Discord](https://img.shields.io/discord/779317239722672128?label=Discord)](https://discord.gg/UrWG3R683d)
[![Docker Pulls](https://badgen.net/docker/pulls/megoru/noticeme?icon=docker&label=pulls)](https://hub.docker.com/r/megoru/noticeme/)
[![Docker Image Size](https://badgen.net/docker/size/megoru/noticeme?icon=docker&label=image%20size)](https://hub.docker.com/r/megoru/noticeme)

## LICENSE

This work is licensed under a [GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.en.html)

## Add bot to your guild
[boticord.top](https://boticord.top/bot/1039911109911658557)

## Technologies used

- Java 20
- MariaDB
- Docker
- Spring Boot
- Hibernate
- Maven

## Running on your server
1. Move `docker-compose.yml` at the root `/root` VPS server.
2. Fill it with your data.
3. Import tables to your `MariaDB`! : `NoticeMe.sql`
4. Launch the container: `docker-compose up -d`
5. If you need to update the repository: `docker-compose pull`
6. If you need to stop: `docker-compose stop`

## Copyright Notice

1. The bot is made using the library: [JDA](https://github.com/DV8FromTheWorld/JDA)

## Privacy Policy

Here you can read more about what we store and how we store it. [Privacy Policy](https://github.com/megoRU/NoticeMe/blob/main/.github/privacy.md)