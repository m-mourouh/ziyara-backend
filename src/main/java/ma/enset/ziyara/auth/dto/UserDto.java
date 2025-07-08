package ma.enset.ziyara.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.enset.ziyara.auth.entity.UserRole;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole role;
    private String profileImageUrl;
    private String bio;
    private Set<String> preferences;
    private LocalDateTime createdAt;
    private Boolean enabled;
}
