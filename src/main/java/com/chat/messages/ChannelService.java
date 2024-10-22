package com.chat.messages;

import com.chat.common.repository.*;
import com.chat.common.security.SecurityContextHelper;
import com.chat.messages.dto.ChannelResponse;
import com.chat.user.UserProfileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService {
    private final SecurityContextHelper securityContextHelper;
    private final UserProfileService userProfileService;
    private final UserChannelRepository userChannelRepository;
    private final ChannelRepository channelRepository;

    public ChannelService(
            SecurityContextHelper securityContextHelper,
            UserProfileService userProfileService,
            UserChannelRepository userChannelRepository,
            ChannelRepository channelRepository) {
        this.securityContextHelper = securityContextHelper;
        this.userProfileService = userProfileService;
        this.userChannelRepository = userChannelRepository;
        this.channelRepository = channelRepository;
    }

    public List<ChannelResponse> getChannels() {
        String email = securityContextHelper.principal().email();
        UserProfile userProfile = userProfileService.getUserProfile(email);
        List<UserChannel> userChannels = userChannelRepository.findByIdUserId(userProfile.id());
        List<String> channelIds = userChannels.stream()
                .map(UserChannel::id)
                .map(UserChannelId::channelId)
                .distinct()
                .toList();
        List<Channel> channels = channelRepository.findByIdIn(channelIds);
        // TODO: Given a list of channel, pull all conversation and transform to ChannelResponse object

        return List.of();
    }
}
