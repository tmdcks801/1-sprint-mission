package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Entity;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@Entity
public abstract class BaseUpdateEntity extends BaseEntity {

  @LastModifiedDate
  private Instant updatedAt;


}
