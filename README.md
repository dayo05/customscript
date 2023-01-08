# Custom Script
## Custom Script는 누구나 쉽게 Minecraft GUI/HUD를 만들 수 있게 해주는 모드입니다.
* 이 모드는 스크립트를 이용하여 GUI/HUD를 제작할 수 있게 해주며, 이 스크립트 작성을 조금 더 쉽게 하기 위하여 별도의 [편집기](https://github.com/dayo05/Script-Maker)도 갖추고 있습니다.
> 편집기에 관한 설명은 링크를 참고해주세요 [링크](https://github.com/dayo05/Script-Maker)
* **중요!! [1.12.2버전을 사용하시는 경우 다음 내용을 참고해주세요.](https://github.com/dayo05/Script-Maker#minecraft-1122-%EC%82%AC%EC%9A%A9%EC%8B%9C%EC%9D%98-%EA%B2%BD%EA%B3%A0)**

## Download
| Version | State | Link |
| --- | --- | --- |
| 1.12.2 Forge | Deprecated | [Link](TODO) |
| 1.16.5 Forge | Supported | [Link](TODO) |
| 1.18.2 Fabric | On Development | TODO |

## Basic Information
### 명령어
#### /cs open <player> <script> [open position]
* 지정된 플레이어(들)의 스크립트를 실행합니다. 

> 만일 open position이 지정되지 않았다면, default로 지정합니다.(처음 Editor을 키면 자동 생성되는 블럭입니다)
#### /cs hud (enable|disable) <script> <player>
* 지정된 플레이어(들)에게 hud를 표시합니다. open position은 항상 hud로 고정되있습니다.
#### /cs cache (font|image) clear <player>
* 비권장
* 지정된 플레이어(들)에게 캐싱된 이미지 혹은 폰트를 초기화합니다.
#### /cs value <player> <value-name> <value>
* 지정된 플레이어(들)에게 해당 값을 부여합니다. 상세 내용은 동적 수치를 참고해주세요.
#### /cs internal-value <value-name> <value>
* internal-value에 해당 값을 부여합니다. 상세 내용은 동적 수치를 참고해주세요.
#### /cs show-value <player> <value-name>
* 지정된 플레이어(들)에게 부여된 값을 확인합니다. 상세 내용은 동적 수치를 참고해주세요.
#### /cs show-internal-value <value-name>
* internal-value에 부여된 값을 확인합니다. 상세 내용은 동적 수치를 참고해주세요.

### 파일들의 위치
* 각 파일들은 지정된 위치에 있어야 합니다.

| 파일 종류 | 저장 위치 |
| --- | --- |
| Script | .minecraft/customscript/ |
| Font | .minecraft/assets/fonts |
| Image | .minecraft/assets/images |
| Video | .minecraft/assets/videos |

예를 들면, 만일 Button Block에서 정의한 Button Image가 test.png라면, .minecraft 밑의 assets 밑에 images 밑에 이 파일을 위치하면 됩니다.

> 폴더가 없다면 새로 만드세요!

또한 파일들은 인터넷을 통해서도 받아올 수 있습니다. 사용법은 파일이름에 웹페이지 링크 넣으면 됩니다. 상세한 내용은 Technical information을 참고해주세요.

### 동적 수치/텍스트
* GUI를 구성하다 보면, 동적으로 텍스트를 변경하거나, 버튼의 위치를 변경하는 등의 행동을 할 필요가 있습니다.
* 텍스트 내부에 `{변수명}`과 같은 형식으로 입력을 하면, 만일 이에 해당되는 변수가 있다면 그 값으로 변경해줍니다.

> Test {test}입니다. 가 있다면,
>
> /cs internal-value test "12" 명령어 실행 시 저 문자열은
>
> Test 12입니다. 로 변경되게 됩니다.
>
> 이 정보는 GUI가 열려있는 동안에도 자동 적용됩니다.

* 문자열에 스페이스가 없는 경우엔 양쪽 끝의 "를 생략 가능합니다.

* Internal-value와 일반 value간의 차이는, internal-value는 전체를 위한 수치이며, 만일 일반 value가 동시에 존재한다면, internal-value는 무시됩니다. 그러나, 일반 value는 플레이어별 수치입니다.

* 보안을 위하여 실제 값들은 GUI가 열려있는 상태에만 갱신됩니다. 이를 위하여 서버의 config 편집이 필요합니다.
```
Script=test1.sc
>Value=test1
>Value=test2

Script=test2.sc
>Value=test1
>Value=test3
```
이 설정 파일은 test1.sc는 test1, test2의 변경을 플레이어에게 전달하고, test2.sc는 test1, test3의 변경을 플레이어에게 전달하라는 의미가 됩니다.
### 랜더링 시스템
* Custom Script는 자체 랜더링 시스템을 보유하고 있습니다. 상세 내용은 Technical Information을 참고해 주세요.
* 랜더링 단계는 크게 Pre, Main, Post 3개로 나뉩니다. Pre, Main, Post 순서대로 랜더링 되며, 먼저 랜더링된 친구는 밑에 깔립니다.
* 만일 동일한 우선순위의 항목이 2개 이상 존재하는 경우 먼저 추가된 친구가 먼저 랜더링됩니다.

### Server side command
* Server side command는 매우 조심히 다루어야 합니다. 이 명령어는 서버 콘솔의 권한으로 명령어를 실행해 줍니다.
* 이 기능은 흑마법입니다. 여러 명령어를 쉽게 작동할 수 있게 해주나, 만일, 플레이어가 임의로 스크립트를 op Dayo05와 같은 절대 해서는 않되는 명령어도 실행이 가능하게 됩니다.(서버 콘솔은 op입니다.)
* 이 기능의 사용을 권장하지 않으나, 굳이 실행하고 싶다면, 설정파일에 `Allow-server-side-command=True` 줄을 맨 밑에 추가해주세요.


## Technical Information
TODO
### Resource API
### Dynamic Value
### Rendering Queue
### API Support
