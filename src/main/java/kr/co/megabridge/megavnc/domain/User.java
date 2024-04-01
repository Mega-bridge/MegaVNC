package kr.co.megabridge.megavnc.domain;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor
public class User implements UserDetails {

    private String username;
    private String password;
    private Set<String> roles;

    public static User createUser(
            String username,
            String rawPassword,
            Set<String> roles,
            PasswordEncoder encoder
    ) {
        User user = new User();
        user.username = username;
        user.password = encoder.encode(rawPassword);
        user.roles = roles;

        return user;
    }
    //사용자에게 부여된 권한 목록 반환,GrantedAuthority 인터페이스를 구현하는 객체들의 컬렉션 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
    //사용자 계정이 만료되었는지 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    //사용자 계정이 잠겨 있는지 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    //사용자의 인증정보가 만료되었는지 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    //사용자 계정이 활성화되어 있는지 여부
    @Override
    public boolean isEnabled() {
        return true;
    }
}
