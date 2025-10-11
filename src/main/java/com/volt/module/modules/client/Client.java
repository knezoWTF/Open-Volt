package com.volt.module.modules.client;

import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;

public final class Client extends Module {
    public static final BooleanSetting title = new BooleanSetting("Title", true);

    public Client() {
        super("Client", "Settings for the client", -1, Category.CLIENT);
        this.addSettings(title);
    }

    public boolean getTitle() {
        return title.getValue();
    }
}
