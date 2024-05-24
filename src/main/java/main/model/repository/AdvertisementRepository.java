package main.model.repository;

import jakarta.transaction.Transactional;
import main.model.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Integer> {

    @Transactional
    @Modifying
    void deleteAdvertisementByGuildId(Long guildId);
}