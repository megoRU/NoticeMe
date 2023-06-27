package main.model.repository;

import main.model.entity.Server;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuildRepository extends JpaRepository<Server, Long> {

    @Nullable
    Server findServerByGuildIdLong(Long guildIdLong);
}