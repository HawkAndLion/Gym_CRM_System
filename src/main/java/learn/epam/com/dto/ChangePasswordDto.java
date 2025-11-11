package learn.epam.com.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

@Schema(
        description = "Change password request",
        requiredProperties = {"oldPassword", "newPassword"}
)
public class ChangePasswordDto {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;

    public ChangePasswordDto() {
    }

    public ChangePasswordDto(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ChangePasswordDto that = (ChangePasswordDto) object;
        return Objects.equals(oldPassword, that.oldPassword) && Objects.equals(newPassword, that.newPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldPassword, newPassword);
    }
}
