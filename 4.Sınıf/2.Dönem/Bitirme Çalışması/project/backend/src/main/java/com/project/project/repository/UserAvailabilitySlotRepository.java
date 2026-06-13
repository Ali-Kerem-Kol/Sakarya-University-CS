package com.project.project.repository;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.project.entity.UserAvailabilitySlot;

/**
 * Data access layer for user availability slots.
 */
public interface UserAvailabilitySlotRepository extends JpaRepository<UserAvailabilitySlot, Long> {

    @Query("""
            select count(s) > 0 from UserAvailabilitySlot s
            where s.profile.userAccount.id = :userId
              and s.dayOfWeek = :dayOfWeek
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
    boolean existsOverlappingSlot(
            @Param("userId") Long userId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    java.util.List<UserAvailabilitySlot> findByProfileUserAccountId(Long userId);

    java.util.Optional<UserAvailabilitySlot> findByIdAndProfileUserAccountId(Long id, Long userId);

    boolean existsByProfileUserAccountId(Long userId);

    @Query("""
            select s.profile.userAccount.id, count(s)
            from UserAvailabilitySlot s
            where s.profile.userAccount.id in :userIds
            group by s.profile.userAccount.id
            """)
    java.util.List<Object[]> countByUserIds(@Param("userIds") java.util.List<Long> userIds);
}
