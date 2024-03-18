package kr.co.megabridge.megavnc.security;

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
        //Fixme: salt처리안하고 단순 해싱 처리라 보안적으로 안좋음, springSecurity 5.0 이상부터 값에 타입을 지정해야 하기 때문에 단순 스티링으로 저장하면 안됨, 서버가 갑자기 재실행되는 에러가 발생함
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
