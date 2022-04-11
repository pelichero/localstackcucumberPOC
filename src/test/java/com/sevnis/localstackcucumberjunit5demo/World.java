package com.sevnis.localstackcucumberjunit5demo;

import com.amazonaws.services.s3.model.Bucket;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("cucumber-glue")
@Getter
@Setter
public class World {

  private Bucket bucket;
}
