package cc.tweaked.prometheus;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


class CommentedConfigSpec extends ConfigSpec {
    private final Map<List<String>, String> comments = new HashMap<>();

    public void comment(String path, String comment) {
        comments.put(StringUtils.split(path, '.'), comment);
    }

    @Override
    public int correct(Config config, ConfigSpec.CorrectionListener listener) {
        int corrections = super.correct(config, listener);
        if (config instanceof CommentedConfig commented) {
            for (Map.Entry<List<String>, String> entry : comments.entrySet()) {
                commented.setComment(entry.getKey(), entry.getValue());
            }
        }
        return corrections;
    }
}
