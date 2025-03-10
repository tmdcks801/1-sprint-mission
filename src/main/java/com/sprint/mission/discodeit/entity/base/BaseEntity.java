package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
  @Id
  private UUID id;
  @CreatedDate
  private Instant createdAt;

  public BaseEntity(){
    this.id=UUID.randomUUID();
  }

}
