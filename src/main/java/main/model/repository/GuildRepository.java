package main.model.repository;

import main.model.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildRepository extends JpaRepository<Server, Long> {

}