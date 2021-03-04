package io.example.authorization.domain.entity.partner;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.example.authorization.domain.entity.common.MetaEntity;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(
        name = "tb_client"
        , uniqueConstraints = {
                @UniqueConstraint(name = "CLIENT_ID_UNIQUE", columnNames = "client_id")
            }
        )
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class ClientEntity extends MetaEntity implements ClientDetails {

    @Id
    @Column(name = "client_id")
    private String clientId;

    @Column(name = "resource_ids")
    private String resourceIds;

    @Column(name = "client_secret")
    private String clientSecret;

    private String scope;

    @JsonIgnore
    @Column(name = "authorized_grant_types")
    private String authorizedGrantTypes;

    private String authorities;

    @JsonIgnore
    @Column(name = "access_token_validity")
    private int accessTokenValidity;

    @JsonIgnore
    @Column(name = "refresh_token_validity")
    private int refreshTokenValidity;

    @JsonIgnore
    @Column(name = "additional_information")
    private String additionalInformation;

    @JsonIgnore
    @Column(name = "auto_approve")
    private boolean autoApprove;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "partner_no"
            , nullable = false
            , foreignKey = @ForeignKey(name = "TB_CLIENT_PARTNER_NO_FOREIGN_KEY")
    )
    private PartnerEntity partnerEntity;

    /**
     * 특정 사용자에 클라이언트 정보 발급
     * @param partnerEntity 클라이언트 발급 대상 사용자 객체
     */
    public void setPublishedClientInfo(PartnerEntity partnerEntity){
        this.clientId = UUID.randomUUID().toString();

        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        this.clientSecret = passwordEncoder.encode(clientId);

        this.scope = "read";
        this.authorizedGrantTypes = "password,refresh_token";
        this.authorities = PartnerRole.STARTER.name();
        this.accessTokenValidity = 43200; // 12시간
        this.refreshTokenValidity = 86400; // 24시간

        partnerEntity.publishedClientInfo();
        this.partnerEntity = partnerEntity;
    }

    @Override
    public String getClientId() {
        return clientId;
    }
    @Override
    public Set<String> getResourceIds() {
        if (resourceIds == null) return null;
        String[] s = resourceIds.split(",");
        return new HashSet<>(Arrays.asList(s));
    }

    @Override
    public boolean isSecretRequired() {
        return clientSecret != null;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public boolean isScoped() {
        return scope != null;
    }

    @Override
    public Set<String> getScope() {
        if (scope == null) return null;
        String[] s = scope.split(",");
        return new HashSet<>(Arrays.asList(s));
    }

    @Override
    public Set<String> getAuthorizedGrantTypes() {
        if (authorizedGrantTypes == null) return null;
        String[] s = authorizedGrantTypes.split(",");
        return new HashSet<>(Arrays.asList(s));
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        return null;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        if (authorities == null) return new ArrayList<>();
        return AuthorityUtils.createAuthorityList(authorities.split(","));
    }

    @Override
    public Integer getAccessTokenValiditySeconds() {
        return accessTokenValidity;
    }

    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return refreshTokenValidity;
    }

    @Override
    public boolean isAutoApprove(String scope) {
        return autoApprove;
    }

    @Override
    public Map<String, Object> getAdditionalInformation() {
        return null;
    }
}