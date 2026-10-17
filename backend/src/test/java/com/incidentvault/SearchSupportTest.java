package com.incidentvault;

import com.incidentvault.service.SearchSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchSupportTest {

    @Test
    void normalizeReturnsNullForNull() {
        assertThat(SearchSupport.normalize(null)).isNull();
    }

    @Test
    void normalizeReturnsNullForBlank() {
        assertThat(SearchSupport.normalize("")).isNull();
        assertThat(SearchSupport.normalize("   ")).isNull();
        assertThat(SearchSupport.normalize("\t\n ")).isNull();
    }

    @Test
    void normalizeTrimsQuery() {
        assertThat(SearchSupport.normalize("  redis failover  ")).isEqualTo("redis failover");
    }

    @Test
    void normalizeLeavesInnerWhitespaceAlone() {
        assertThat(SearchSupport.normalize("kafka  lag")).isEqualTo("kafka  lag");
    }
}
