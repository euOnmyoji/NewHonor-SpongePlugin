package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.data.HonorData;
import com.github.euonmyoji.newhonor.api.manager.HonorManager;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import me.rojo8399.placeholderapi.Token;
import me.rojo8399.placeholderapi.impl.utils.TextUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
public final class PlaceholderManager {
    private static final String VALUE_T = "value";
    private static final String STR_T = "strvalue";
    private static final String ID_T = "usingid";
    private static PlaceholderManager instance;
    private PlaceholderService service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);


    private PlaceholderManager() {
        service.loadAll(this, NewHonor.plugin).forEach(builder -> {
            if (NewHonor.NEWHONOR_ID.equals(builder.getId())) {
                try {
                    builder.description("newhonor text").version("1.5").author("yinyangshi").plugin(NewHonor.plugin)
                            .addTokens(VALUE_T, STR_T, ID_T)
                            .buildAndRegister();
                } catch (Exception e) {
                    NewHonor.logger.warn("offer PAPI failed", e);
                }
            }
        });
    }

    public static PlaceholderManager getInstance() {
        if (instance == null) {
            instance = new PlaceholderManager();
        }
        return instance;
    }

    private static String removeInvalidColorCode(String s) {
        if (s.startsWith("&r")) {
            s = s.substring(2);
        }
        if (s.startsWith("&f")) {
            s = s.substring(2);
        }
        if (s.startsWith("&r")) {
            s = s.substring(2);
        }
        if (s.length() > 2 && s.charAt(s.length() - 2) == '&') {
            s = s.substring(0, s.length() - 2);
        }
        return s;
    }

    public Text parseText(Text text, Player p) {
        return service.replacePlaceholders(text, p, p);
    }

    public Text parseTextOnlyColor(Text text, Player p) {
        TextTemplate textTemplate = TextUtils.toTemplate(text, PlaceholderService.DEFAULT_PATTERN);
        Map<String, ?> map = service.fillPlaceholders(textTemplate, p, p).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, node -> {
                    Object v = node.getValue();
                    if (v == null) {
                        return node.getKey();
                    }
                    if (v instanceof Text) {
                        String s = TextSerializers.FORMATTING_CODE.serialize(((Text) v));
                        s = removeInvalidColorCode(s);
                        return s;
                    } else if (v instanceof String) {
                        return removeInvalidColorCode(((String) v));
                    }
                    return v;
                }));
        String s = TextSerializers.FORMATTING_CODE.serialize(textTemplate.apply(map).build());
        s = removeInvalidColorCode(s);
        return TextSerializers.FORMATTING_CODE.deserialize(s);
    }

    @Placeholder(id = NewHonor.NEWHONOR_ID)
    public Object getNewHonorText(@Source User user, @Nullable @Token(fix = true) String token) {
        HonorData value = Sponge.getServiceManager().provideUnchecked(HonorManager.class).getUsingHonor(user.getUniqueId());
        if (value != null) {
            if (token == null) {
                return value.getValue(user.getPlayer().orElse(null));
            }
            switch (token) {
                case ID_T: {
                    return value.getId();
                }
                case STR_T: {
                    return value.getStrValue();
                }
                case VALUE_T: {
                    return value.getValue(user.getPlayer().orElse(null));
                }
                default: {
                    //not to offer value though default
                    break;
                }
            }
        }

        return null;
    }
}
