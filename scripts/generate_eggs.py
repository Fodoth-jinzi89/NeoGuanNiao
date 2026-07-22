import json
from pathlib import Path


MODID = "neoguanniao"

OUTPUT = Path("output/models/item/bird_egg")


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


MODEL_RARITIES = {
    0: "common",
    1: "uncommon",
    2: "rare",
    3: "epic",
    4: "legendary",
    5: "ancient",
    6: "unique",
    999: "hidden"
}


GENDERS = {
    0: "male",
    1: "female"
}


def texture(path):
    return f"{MODID}:item/bird_egg/{path}"



def generate_model(
        skin_rarity,
        model_rarity,
        gender
):

    return {
        "parent": "minecraft:item/generated",

        "textures": {

            # 蛋本体（skin rarity）
            "layer0":
                texture(
                    f"bird_egg_{skin_rarity}"
                ),

            # 模型稀有度（model rarity）
            "layer1":
                texture(
                    f"rarity/{model_rarity}"
                ),

            # 性别
            "layer2":
                texture(
                    f"gender/{gender}"
                )
        }
    }



def generate_main():

    overrides = []


    for skin_id, skin_name in SKIN_RARITIES.items():

        for model_id, model_name in MODEL_RARITIES.items():

            for gender_id, gender_name in GENDERS.items():


                # 默认模型不用 override
                if (
                    skin_id == 0
                    and model_id == 0
                    and gender_id == 0
                ):
                    continue


                overrides.append(
                    {
                        "predicate": {

                            # skin rarity
                            "neoguanniao:rarity":
                                skin_id,


                            # model rarity
                            "neoguanniao:model_rarity":
                                model_id,


                            # gender
                            "neoguanniao:gender":
                                gender_id
                        },


                        "model":
                            f"{MODID}:item/bird_egg/"
                            f"bird_egg_"
                            f"{skin_name}_"
                            f"{model_name}_"
                            f"{gender_name}"
                    }
                )


    return {

        "parent":
            "minecraft:item/generated",


        "textures": {

            "layer0":
                texture(
                    "bird_egg_common"
                ),

            "layer1":
                texture(
                    "rarity/common"
                ),

            "layer2":
                texture(
                    "gender/male"
                )
        },


        "overrides":
            overrides
    }



def save(path, data):

    path.parent.mkdir(
        parents=True,
        exist_ok=True
    )


    with open(
        path,
        "w",
        encoding="utf-8"
    ) as f:

        json.dump(
            data,
            f,
            indent=2,
            ensure_ascii=False
        )



def main():

    # 主模型
    save(
        OUTPUT / "bird_egg.json",
        generate_main()
    )


    # 所有组合模型
    for skin_id, skin_name in SKIN_RARITIES.items():

        for model_id, model_name in MODEL_RARITIES.items():

            for gender_id, gender_name in GENDERS.items():

                save(
                    OUTPUT /
                    f"bird_egg_{skin_name}_{model_name}_{gender_name}.json",

                    generate_model(
                        skin_name,
                        model_name,
                        gender_name
                    )
                )


if __name__ == "__main__":
    main()