# NoticeMe

[![Java CI](https://github.com/megoRU/NoticeMe/actions/workflows/ci_cd.yml/badge.svg)](https://github.com/megoRU/NoticeMe/actions/workflows/ci_cd.yml)
[![Docker Pulls](https://badgen.net/docker/pulls/megoru/noticeme?icon=docker\&label=pulls)](https://hub.docker.com/r/megoru/noticeme/)
[![Docker Image Size](https://badgen.net/docker/size/megoru/noticeme?icon=docker\&label=image%20size)](https://hub.docker.com/r/megoru/noticeme)

A Discord bot that allows users to subscribe to specific members and receive automated notifications on the server when those members join a voice channel.

---

## ✨ Features

* Subscribe to users and get notified when they join a voice channel
* Slash command support
* Persistent storage with MariaDB
* Dockerized and CI/CD-ready

---

## 🚀 Quick Start

### Add the bot to your server

[Click here to invite](https://discord.com/oauth2/authorize?client_id=1039911109911658557)

### Run with Docker

1. Place `docker-compose.yml` on your VPS (`/root` or other directory).
2. Configure it with your environment variables.
3. Import `NoticeMe.sql` into your MariaDB instance.
4. Start the bot:

```bash
docker-compose up -d
```

For an existing installation, apply `NoticeMe-migration-2026-10-18.sql` to the
existing database before deploying this version. It adds the `gender` table and
allows the `FR` language value without dropping existing data.

5. Update the container:

```bash
docker-compose pull && docker-compose up -d
```

6. Stop the bot:

```bash
docker-compose stop
```

---

## 🛠 Tech Stack

* Java 20
* Spring Boot
* Hibernate
* MariaDB
* Docker
* Maven
* [JDA](https://github.com/DV8FromTheWorld/JDA)

---

## 📄 License

This project is licensed under the [GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.en.html).

---

## 🔒 Privacy

Details on how data is stored and used are described in the [Privacy Policy](https://github.com/megoRU/NoticeMe/blob/main/.github/privacy.md).
