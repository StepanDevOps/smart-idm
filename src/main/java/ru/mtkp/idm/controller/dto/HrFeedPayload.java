package ru.mtkp.idm.controller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HrFeedPayload {
    private Long userId;
    private String login;
    private String department;
}