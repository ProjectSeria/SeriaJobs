package id.seria.armorset.hook;

import id.seria.armorset.skill.ArmorSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;

public class MythicLibSkillHandler extends SkillHandler<SimpleSkillResult> {

    private final String id;
    private final ArmorSkill skill;

    public MythicLibSkillHandler(String id, ArmorSkill skill) {
        super(id);
        this.id = id;
        this.skill = skill;
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata metadata) {
        return new SimpleSkillResult(true);
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata metadata) {
        // Handled through the SeriaArmorSet skill engine event listeners
    }

    public ArmorSkill getSkill() {
        return skill;
    }
}