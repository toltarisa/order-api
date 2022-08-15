package com.isatoltar.order.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {

    @NotNull
    @Positive
    Integer tableNo;

    @NotNull
    @Size(max = 255)
    String flavor;

    @NotNull
    @Size(max = 255)
    String crust;

    @NotNull
    @Size(max = 255)
    String size;
}
