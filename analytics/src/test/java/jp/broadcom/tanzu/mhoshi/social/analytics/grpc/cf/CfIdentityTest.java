package jp.broadcom.tanzu.mhoshi.social.analytics.grpc.cf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CfIdentityTest {

    @Mock
    private CfCertificate clientCert;

    @Mock
    private CfCertificate serverCert;

    @Test
    void getAuthorities_ShouldReturnRoleApp_WhenCertificatesMatchSpace() {
        // Arrange
        when(clientCert.matchesSpace(serverCert)).thenReturn(true);
        CfIdentity identity = new CfIdentity(clientCert, serverCert);

        // Act
        Collection<? extends GrantedAuthority> authorities = identity.getAuthorities();

        // Assert
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_APP");
    }

    @Test
    void getAuthorities_ShouldReturnEmpty_WhenCertificatesDoNotMatchSpace() {
        // Arrange
        when(clientCert.matchesSpace(serverCert)).thenReturn(false);
        CfIdentity identity = new CfIdentity(clientCert, serverCert);

        // Act
        Collection<? extends GrantedAuthority> authorities = identity.getAuthorities();

        // Assert
        assertThat(authorities).isEmpty();
    }

    @Test
    void getUsername_ShouldReturnClientSubject() {
        // Arrange
        String expectedSubject = "CN=app:123,OU=space:abc";
        when(clientCert.subject()).thenReturn(expectedSubject);
        CfIdentity identity = new CfIdentity(clientCert, serverCert);

        // Act
        String username = identity.getUsername();

        // Assert
        assertThat(username).isEqualTo(expectedSubject);
    }

    @Test
    void getPassword_ShouldReturnEmptyString() {
        // Arrange
        CfIdentity identity = new CfIdentity(clientCert, serverCert);

        // Act & Assert
        assertThat(identity.getPassword()).isEmpty();
    }

    @Test
    void equals_ShouldBeTrue_WhenSubjectsAreSame() {
        // Arrange
        CfIdentity identity1 = new CfIdentity(new CfCertificate("CN=test-app"), serverCert);
        CfIdentity identity2 = new CfIdentity(new CfCertificate("CN=test-app"), serverCert);

        // Act & Assert
        assertThat(identity1).isEqualTo(identity2);
        assertThat(identity1).hasSameHashCodeAs(identity2);
    }

    @Test
    void equals_ShouldBeFalse_WhenSubjectsAreDifferent() {
        // Arrange
        CfIdentity identity1 = new CfIdentity(new CfCertificate("CN=app-A"), serverCert);
        CfIdentity identity2 = new CfIdentity(new CfCertificate("CN=app-B"), serverCert);

        // Act & Assert
        assertThat(identity1).isNotEqualTo(identity2);
    }
}