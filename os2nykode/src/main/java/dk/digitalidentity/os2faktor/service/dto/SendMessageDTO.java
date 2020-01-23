package dk.digitalidentity.os2faktor.service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SendMessageDTO {
	private String content;
	private List<String> numbers;
}
