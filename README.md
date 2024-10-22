# 안전지킴이 Android Application
## LG Embedded 자동차 모빌리티 부문

# :oncoming_automobile: UWB 기반 골목길 교통사고 안전 서비스앱 SAFEGUARD

<br>

## 프로젝트 소개

- SAFEGUARD는 좁은 길을 지나는 보행자를 향해 사각지대에서 접근해오는 차량을 UWB 통신 기반으로 감지할 수 있음.
- 감지가 되었을 때, 보행자에게 알림 및 진동을 띄워 위험을 알릴 수 있음.

<br>

## 1. 개발 환경

- Front : Kotlin
- Back-end : Node.js
- 버전 관리 : Github
- 협업 툴 : Notion, Github 
- 서비스 배포 환경 : Android Studio
  
<br>

## 2. 채택한 개발 기술과 브랜치 전략

### Kotlin

- 코틀린 언어를 사용해 개발하여, 간결하고 안전한 코드 작성을 통해 유지보수성과 개발 생산성을 높임.
- 
    
### Coroutines

- 비동기 작업을 간단하게 처리하기 위해 코루틴을 사용함.
- 

### Android-Estimote-UWB-SDK

- Core Ultra Wideband(UWB) Jetpack 라이브러리를 활용하여 UWB 지원 Android 기기와 비콘을 검색, 연결하기 위해 사용함.
- 각각의 비콘과 Connect 및 Disconnect를 반복하며 각 비콘과의 거리를 측정하여 사용자의 위치를 특정하는 방식으로 사용함.
  - Connect 및 Disconnect를 반복한 이유는 Estimote Beacon과 휴대폰의 다중 연결이 지원되지 않기 때문 

### 브랜치 전략

- Git-flow 전략을 기반으로 main, dev 브랜치와 1/aos 보조 브랜치를 운용했습니다.
- main, dev, Feat 브랜치로 나누어 개발을 하였습니다.
    - **main** 브랜치는 배포 단계에서만 사용하는 브랜치입니다.
    - **dev** 브랜치는 개발 단계에서 git-flow의 master 역할을 하는 브랜치입니다.
    - **1/aos** 브랜치는 기능 단위로 독립적인 개발 환경을 위하여 사용하고 merge 후 각 브랜치를 삭제함.

<br>

## 3. 프로젝트 구조

```
│  AndroidManifest.xml 
├─java
│  └─com
│      └─uwb
│          └─safeguard
│              ├─config
│              │      ApplicationClass.kt
│              │      BaseActivity.kt
│              │      BaseFragment.kt
│              │      BaseResponse.kt
│              │      XAccessTokenInterceptor.kt
│              │      
│              ├─src
│              │  │  GpsService.kt
│              │  │  MainActivity.kt
│              │  │  MainActivityInterface.kt
│              │  │  MainActivityRetrofitInterface.kt
│              │  │  MainService.kt
│              │  │  SplashActivity.kt
│              │  │  SSEClient.kt
│              │  │  
│              │  └─model
│              │          CarRes.kt
│              │          CarResponse.kt
│              │          UserDeleteReq.kt
│              │          UserRes.kt
│              │          
│              └─util
│                      Alarm.kt
│                      CustomDialog.kt
│                      Foreground.kt
│                      
└─res
    ├─drawable
    │      ic_launcher_background.xml
    │      ic_launcher_foreground.xml
    │      ripple_btn.xml
    │      start_btn.xml
    │      
    ├─layout
    │      activity_main.xml
    │      activity_splash.xml
    │      dialog_alarm.xml 
```

<br>

## 4. 개발 기간 및 작업 관리

### 개발 기간

- 전체 개발 기간 : 2024-09-20 ~ 2024-10-22
- UI 구현 : 2024-09-20 ~ 2022-09-23
- 기능 구현 : 2024-09-24 ~ 2024-10-22

<br>

### 작업 관리

- GitHub 1/aos 기능 브랜치에 커밋하며 진행 상황을 공유했습니다.
- 주간회의를 진행하며 작업 순서와 방향성에 대한 고민을 나누고 Notion에 기록하였습니다.

<br>

## 5. 페이지별 기능

### [초기화면]
- 서비스 접속 초기화면으로 splash 화면이 잠시 나온 뒤 메인 페이지가 나타납니다.

| 초기화면 |
|----------|
|![splash](https://user-images.githubusercontent.com/112460466/210172920-aef402ed-5aef-4d4a-94b9-2b7147fd8389.gif)|

<br>

### [프로필]

#### 1. 내 프로필
- 상단 프로필란에 프로필 수정과 상품 등록 버튼이 나타납니다.
- 판매중인 상품란에는 사용자가 판매하는 상품이 등록되며, 판매중인 상품이 없을 경우에는 영역 자체가 나타나지 않습니다.
- 게시글란은 상단의 리스트형과 앨범형 두 개의 버튼을 통해서 나누어 볼 수 있습니다.
    - 리스트형의 경우, 사용자가 작성한 글 내용과 이미지, 좋아요와 댓글의 수를 보여줍니다.
    - 앨범형의 경우, 사용자 게시글 중 이미지가 있는 글만 필터링해 바둑판 배열로 보여줍니다.
- 게시글을 클릭하면 각 게시글의 상세페이지로 이동합니다.

| 리스트형 & 앨범형 게시글 | 팔로잉 & 팔로워 리스트 |
|----------|----------|
|![myProfile](https://user-images.githubusercontent.com/112460466/210380492-40560e0b-c306-4e69-8939-cc3e7dc3d8fe.gif)|![followList](https://user-images.githubusercontent.com/112460466/210380539-d09b0bd7-0b61-4b22-85fa-f75e6bcecb68.gif)|

<br>

#### 2. 타 유저의 프로필
- 버튼을 클릭해 해당 사용자를 팔로우 또는 언팔로우할지 결정할 수 있으며 팔로워 수의 변화가 페이지에 즉시 반영됩니다.

| 팔로우 & 언팔로우 |
|----------|
|![yourProfile](https://user-images.githubusercontent.com/112460466/210380853-04f2d2bd-adab-4786-a8e8-c275ce765071.gif)|

<br>

#### 3. 프로필 수정
- 사용자 프로필 이미지, 이름, 아이디, 소개 중 한 가지를 수정하면 저장 버튼이 활성화됩니다.
- 계정 ID의 유효한 형식 및 중복 검사를 통과하지 못하면 하단에 경고 문구가 나타나며 저장 버튼이 비활성화됩니다.
- 사용자 이름과 소개는 공백으로 시작할 수 없습니다.
- 프로필 수정이 완료되면 내 프로필 페이지로 이동합니다.

| 초기화면 |
|----------|
|![editProfile](https://user-images.githubusercontent.com/112460466/210381212-d67fdf87-b90c-4501-a331-f2a384534941.gif)|

<br>

### [게시글]

#### 1. 게시글 작성
- 글이 입력되거나 사진이 첨부되면 업로드 버튼이 활성화됩니다.
- 최대 세 장까지 이미지 첨부가 가능하며 첨부한 파일을 취소할 수 있습니다.
- 게시글 하단에 업로드 날짜가 표시됩니다.

| 게시글 작성 |
|----------|
|![uploadPost](https://user-images.githubusercontent.com/112460466/210381758-1de5a889-f587-41d2-b200-22c20a970519.gif)|

<br>

#### 2. 게시글 수정 및 삭제
- 자신의 게시글일 경우 모달 버튼을 통해 수정, 삭제가 가능합니다.
- 게시글 삭제 버튼 클릭 시, 게시글을 삭제하고 페이지를 리렌더링하여 삭제된 내용을 페이지에 반영합니다.
- 타 유저의 게시글일 경우 모달 버튼을 통해 신고할 수 있습니다.

| 게시글 수정 & 삭제 |
|----------|
|![editDeletePost](https://user-images.githubusercontent.com/112460466/210382021-da057943-dc21-411e-a1f8-552be0e973bf.gif)|

<br>

#### 3. 좋아요와 댓글
- 좋아요와 댓글 수는 실시간으로 상세 페이지에 반영됩니다.
- 댓글이 몇 분 전에 작성되었는지 표시됩니다.
- 자신의 댓글일 경우 모달 버튼을 통해 삭제가 가능합니다.
- 타 유저의 댓글일 경우 모달 버튼을 통해 신고할 수 있습니다.

| 좋아요 & 댓글 |
|----------|
|![likeComment](https://user-images.githubusercontent.com/112460466/210382217-01d70181-91c3-43db-a1b8-409a612afb1c.gif)|

<br>




