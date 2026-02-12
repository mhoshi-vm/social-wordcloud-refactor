package jp.broadcom.tanzu.mhoshi.social.graphql.grpc.cf;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
class PreferIPNameResolverProvider extends NameResolverProvider {

    @Override
    protected String getScheme() {
        return "prefer-ip";
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        return new PreferIPNameResolver(targetUri);
    }

    @Override
    public String getDefaultScheme() {
        return "";
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }
}