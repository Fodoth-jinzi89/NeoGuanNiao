import json
from pathlib import Path


MODID = "neoguanniao"

OUTPUT = Path("output/models/item/bird_feather")


SKIN_RARITIES = {
    0: "common",
    1: "uncommon",
    2: "rare",
    3: "epic",
    4: "legendary",
    5: "ancient",
    6: "unique",
    999: "hidden"
}

BIRD_TYPES = {
    0: "budgerigar",
    1: "night_heron",
    2: "spotted_dove",
    3: "pigeon",
    4: "sparrow"
}


def texture(path):
    return f"{MODID}:item/bird_feather/{path}"


def generate_model(bird_name, rarity_name):
    return {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": texture(f"bird_feather_{bird_name}_{rarity_name}")
        }
    }


def generate_main():
    overrides = []

    for rarity_id, rarity_name in SKIN_RARITIES.items():
        for bird_id, bird_name in BIRD_TYPES.items():
            # 默认模型（budgerigar + common）不用 override
            if bird_id == 0 and rarity_id == 0:
                continue

            overrides.append(
                {
                    "predicate": {
                        "neoguanniao:bird_type": bird_id,
                        "neoguanniao:rarity": rarity_id
                    },
                    "model": f"{MODID}:item/bird_feather/bird_feather_{bird_name}_{rarity_name}"
                }
            )

    return {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": texture("bird_feather_budgerigar_common")
        },
        "overrides": overrides
    }


def save(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)

    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)


def main():
    # 主模型
    save(
        OUTPUT / "bird_feather.json",
        generate_main()
    )

    # 所有组合模型
    for rarity_id, rarity_name in SKIN_RARITIES.items():
        for bird_id, bird_name in BIRD_TYPES.items():
            save(
                OUTPUT / f"bird_feather_{bird_name}_{rarity_name}.json",
                generate_model(bird_name, rarity_name)
            )


if __name__ == "__main__":
    main()