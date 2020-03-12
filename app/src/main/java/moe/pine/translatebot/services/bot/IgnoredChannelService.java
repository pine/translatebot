package moe.pine.translatebot.services.bot;

import lombok.RequiredArgsConstructor;
import moe.pine.translatebot.pref.IgnoredChannelRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IgnoredChannelService {
    private static final boolean DEFAULT_VALUE = false;

    private final IgnoredChannelRepository ignoredChannelRepository;

    public boolean isIgnored(String channel) {
        return ignoredChannelRepository.get(channel).orElse(DEFAULT_VALUE);
    }
}
