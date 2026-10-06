Friends 워크플로우
Gameplay HUD Team Git Workflow

1. 선택한 Git Workflow 및 선택 이유

우리 팀은 Feature Branch Workflow를 사용한다.

기존에는 각 팀원이 개인 브랜치를 가지고 있었지만, 개인 브랜치가 실제 작업 단위와 명확하게 연결되지 않았고 일부 팀원은 Git을 적극적으로 사용하지 않아 불필요한 복잡성이 발생했다.

따라서 앞으로는 사람을 기준으로 브랜치를 유지하는 대신, 구체적인 작업 또는 기능이 생길 때마다 해당 작업을 위한 Feature Branch를 생성하는 방식으로 전환한다.

Feature Branch Workflow를 선택한 이유는 다음과 같다.


* 여러 작업을 동시에 병렬적으로 진행할 수 있다.
* 브랜치 이름만으로 어떤 작업을 위한 브랜치인지 확인하기 쉽다.
* main을 안정적인 통합 및 기준 브랜치로 유지할 수 있다.
* 기능별 변경사항을 독립적으로 Review하고 Test할 수 있다.
* 장기간 유지되는 개인 브랜치를 줄일 수 있다.
* 다른 팀과의 Dependency 및 작업 범위 중복을 관리하기 쉽다.



2. Branch Strategy

우리 팀은 기본적으로 다음 두 종류의 브랜치를 사용한다.


* main
* feature/


### main

main은 우리 팀의 통합 및 기준 브랜치이다.

다른 팀의 PR이 upstream/main에 Merge되어 변경사항이 발생하면, Integration Manager가 우리 팀의 main을 최신 upstream/main과 동기화한다.

main에서는 직접 개발하지 않는다.

기능 개발, 버그 수정, 문서 수정 등의 모든 작업은 Feature Branch에서 진행한 뒤 Review와 Testing을 거쳐 main에 통합한다.

### feature/

Feature Branch는 하나의 구체적인 작업 또는 목적을 위해 생성한다.

브랜치 이름은 작업을 수행하는 사람의 이름이 아니라 작업 내용을 기준으로 정한다.

예시:


* feature/game-over-score
* feature/high-score-layout
* feature/score-display
* feature/update-git-workflow

Feature Branch는 실제 작업을 시작할 때 최신 main을 기준으로 생성한다.

작업이 완료되고 Review, Testing, Merge까지 정상적으로 끝나면 해당 Feature Branch는 삭제한다.

기본적인 브랜치 생명주기는 다음과 같다.

latest main → feature branch 생성 → 개발 → review/test → main에 merge → feature branch 삭제



3. Commit Rules

하나의 Feature Branch는 하나의 구체적인 작업 또는 목적을 나타낸다.

하나의 브랜치 안에는 여러 Commit이 존재할 수 있다.

각 Commit에는 하나의 논리적인 변경사항만 포함한다.

서로 관련이 없는 변경사항은 하나의 Commit에 함께 넣지 않는다.

하나의 논리적인 작업을 위해 여러 파일을 수정해야 하는 경우에는 여러 파일의 변경사항이 같은 Commit에 포함될 수 있다.

예를 들어 feature/game-over-score 브랜치에는 다음과 같은 Commit들이 포함될 수 있다.


* feat: add final score display
* fix: correct score update logic
* test: add game over score test

작은 작업이라면 하나의 Commit만 존재할 수도 있다.

예:


* Branch: feature/update-git-workflow
* Commit: docs: update Git workflow

Commit은 아주 작은 수정마다 무분별하게 생성하지 않고, 의미 있는 개발 단계가 완료되었을 때 생성한다.


Commit Message Format

Commit Message는 다음 형식을 따른다.

<type>: <short description>

사용할 Type은 다음과 같다.


Type
	의미

feat
	새로운 기능 추가

fix
	버그 수정

docs
	문서 수정

refactor
	기능 변화 없이 코드 구조 개선

test
	테스트 관련 변경

chore
	설정 또는 유지보수 작업



예시:


* feat: add final score display
* fix: correct game over score calculation
* docs: update Git workflow
* refactor: simplify HUD score handling

update, final, fix, work처럼 변경 목적을 알기 어려운 모호한 Commit Message는 사용하지 않는다.



4. Pull Request and Code Review Rules

Internal Pull Request

모든 개발 변경사항은 Pull Request를 통해 우리 팀의 main에 통합한다.

Developer는 다음 조건을 만족했을 때 Feature Branch에서 main으로 Internal PR을 생성한다.


* 할당된 작업이 완료되었을 것
* Developer가 기본적인 동작 확인을 완료했을 것
* 알려진 Dependency를 확인했을 것
* 해당 브랜치가 Integration 가능한 상태일 것

Developer와 Reviewer

작업을 배정할 때 Developer와 Reviewer를 함께 지정한다.

Reviewer는 Developer 본인이 아닌 다른 팀원이어야 하며, 가능하면 해당 기능이나 코드 영역을 이해하고 있는 개발팀원이 담당한다.

Reviewer는 다음 사항을 확인한다.


* 변경사항이 해당 작업의 목적과 일치하는지
* 관련 없는 변경사항이 포함되지 않았는지
* 명백한 기술적 문제가 없는지
* 다른 기능에 부정적인 영향을 줄 가능성이 있는지
* 알려진 Dependency가 적절하게 처리되었는지

Internal PR을 Merge하기 위해서는 최소 1명의 Reviewer Approval이 필요하다.

수정 요청이 있을 경우 Developer는 동일한 Feature Branch에서 수정한 뒤 다시 Review를 받는다.


Tester

Eom Jeong-in이 우리 팀의 지정 Tester를 담당한다.

Tester는 대부분의 기능 테스트를 담당하며 다음 사항을 확인한다.


* 구현된 기능이 의도대로 동작하는지
* 기존 Gameplay 또는 HUD 기능에 문제가 발생하지 않았는지
* 보고된 버그가 정상적으로 수정되었는지
* 해당 기능이 Integration 가능한 상태인지

Tester, Reviewer 또는 다른 담당자가 사정상 역할을 수행할 수 없는 경우에는 해당 역할을 수행할 수 있는 적절한 팀원이 자율적으로 대신할 수 있다.


Integration Manager

Seungwoo가 우리 팀의 Integration Manager를 담당한다.

Integration Manager의 역할은 다음과 같다.


* Review와 Testing이 완료된 Internal PR의 최종 Merge
* 우리 팀 main으로의 Integration 관리
* upstream/main과 우리 팀 main의 동기화
* 여러 변경사항이 같은 영역에 영향을 줄 경우 Integration 과정 조정

다른 팀의 PR이 실제로 upstream/main에 Merge될 때마다 Seungwoo가 우리 팀의 main을 최신 Upstream 상태로 동기화한다.

Seungwoo가 역할을 수행할 수 없는 경우에는 Integration을 처리할 수 있는 다른 팀원이 대신할 수 있다.


Upstream Pull Request

우리 팀은 사전에 정한 Target Work Unit이 완료되고, 필요한 Internal Integration, Review, Testing까지 모두 완료된 상태를 Upstream PR 준비 완료 상태로 본다.

Upstream PR 과정은 앞 팀의 Upstream PR을 처리하는 과정과 우리 팀의 Upstream PR을 제출하는 과정으로 구분한다.

Previous Team Upstream PR Review and Merge
우리 팀의 Upstream PR이 준비되면 먼저 Team Leaders Chat에 준비 완료 사실을 알린다.

이후 바로 앞 팀의 Upstream PR을 다음 역할 분담에 따라 처리한다.


* Seungwoo가 앞 팀 Upstream PR의 Code Review를 담당한다.
* Seungwoo가 Review를 수행하기 어려운 경우 Coordinator Kwanwoo가 Backup Reviewer를 담당한다.
* Reviewer는 앞 팀의 변경사항, Conflict 가능성, 우리 팀 작업과의 Dependency 등을 확인한다.
* Code Review 결과 문제가 없고 Merge Blocking Condition이 존재하지 않는 경우 Kwanwoo가 앞 팀 PR을 upstream/main에 Merge한다.

앞 팀 PR이 Merge된 이후에는 변경된 upstream/main을 우리 팀의 main에 동기화한다.

이 과정에서 Conflict 또는 기능 영향이 발생하면 Section 5의 규칙에 따라 해결하고 영향을 받은 기능을 다시 Test한다.

Our Team Upstream PR Submission
앞 팀 PR의 Merge 및 최신 upstream/main 동기화가 완료되면 우리 팀의 변경사항에 대한 Final Testing을 수행한다.

Conflict 해결과 Final Testing까지 모두 완료되어 Upstream PR을 제출할 수 있는 상태가 되면 Coordinator Kwanwoo가 우리 팀의 PR을 upstream/main으로 생성한다.

우리 팀의 최종 Upstream PR은 Kwanwoo만 생성한다.

PR 생성 후 Team Leaders Chat에 우리 팀 PR이 생성되었음을 알린다.

이후 우리 팀의 Upstream PR에 대한 Review와 Merge는 다음 PR 준비 팀이 담당한다.

전체 Upstream PR 흐름은 다음과 같다.

우리 팀 PR 준비 완료 → Seungwoo가 앞 팀 PR Review → 필요 시 Kwanwoo가 Backup Review → Kwanwoo가 앞 팀 PR Merge → upstream/main 동기화 → Conflict 해결 및 Final Testing → Kwanwoo가 우리 팀 Upstream PR 생성 → 다음 준비 팀이 우리 팀 PR Review/Merge


Direct Push to main

개발 변경사항을 main에 직접 Push하는 것은 허용하지 않는다.

기능 개발, 버그 수정, 문서 수정은 모두 Feature Branch에서 진행한 뒤 PR을 통해 통합한다.

단, 최신 upstream/main과 우리 팀 main을 동기화하기 위한 작업은 예외이며 Integration Manager가 담당한다.



5. Merge Strategy

우리 팀은 일반 Merge를 기본 Integration 방식으로 사용한다.

일반적인 Workflow에서는 Squash Merge 또는 Rebase를 사용하지 않는다.

이를 통해 Feature 개발 과정에서 생성된 개별 Commit 기록을 유지하고, Shared Commit History를 다시 작성하는 상황을 방지하며, 모든 팀원이 동일한 방식으로 작업할 수 있도록 한다.


Feature Branch → main

Internal PR이 Review와 Testing을 통과하면 Seungwoo가 일반 Merge 방식으로 Feature Branch를 main에 Merge한다.

Merge가 정상적으로 완료되면 해당 Feature Branch는 삭제한다.


최신 main을 Feature Branch에 반영하는 경우

Feature Branch에서 최신 main의 변경사항이 필요한 경우 Rebase 대신 Merge를 사용한다.

Feature Branch는 main이 변경될 때마다 즉시 최신화할 필요는 없다.

다음 상황에서는 최신 main을 Feature Branch에 반영한다.


* 최신 변경사항이 해당 Feature에 영향을 주는 경우
* Internal Integration 전에 필요한 경우
* 우리 팀의 Upstream PR을 준비하는 경우

Merge Conflict Resolution

Conflict의 종류에 따라 해결 담당자를 다르게 한다.

Feature Branch와 main 사이의 Conflict
Feature Branch와 최신 main 사이에서 Conflict가 발생하면 해당 Feature를 담당한 Developer가 우선 해결한다.

Conflict 해결 후에는 변경 결과를 다시 Review하고 Test한다.

우리 팀 내부 Feature 간 Conflict
우리 팀의 두 Feature가 서로 Conflict를 일으키는 경우에는 각 Feature를 담당한 Developer들이 함께 해결한다.

한 Developer가 다른 Developer의 의도한 동작을 임의로 제거하거나 변경하지 않는다.

필요한 경우 Integration Manager가 해결 과정을 조정한다.

다른 팀 변경사항과의 Conflict
다른 팀에서 개발한 의미 있는 기능 또는 코드와 Conflict가 발생한 경우에는 우리 팀에서 임의로 어느 변경사항을 선택하지 않는다.

다음 과정을 따른다.

Conflict 확인 → 관련 팀과 소통 → 의도한 동작 및 Dependency 확인 → 해결 방법 합의 → Conflict 해결 → 재테스트

다른 팀의 의미 있는 변경사항을 임의로 덮어쓰지 않는다.

Formatting 또는 Import 순서처럼 프로그램 동작에 영향을 주지 않는 단순 Conflict는 담당 Developer가 직접 해결할 수 있다.


Merge Blocking Conditions

다음 상황에서는 Merge를 진행하지 않는다.


* Reviewer Approval이 없는 경우
* Merge Conflict가 해결되지 않은 경우
* 필수 Test가 실패한 경우
* 확인되지 않은 Dependency가 있는 경우

Reviewer가 수정이 필요하다고 판단한 경우 해당 변경사항을 먼저 수정하고 다시 Review를 받아야 한다.

Merge Conflict가 발생한 경우 위의 Conflict Resolution 규칙에 따라 해결해야 한다.

필수 Test가 실패한 경우 문제를 수정하고 Test를 다시 수행해야 한다.

다른 Feature, 공용 코드 또는 다른 팀 작업과의 Dependency가 확인되지 않은 경우에는 관련 담당자 또는 팀과 먼저 소통하여 Dependency와 작업 범위를 명확히 해야 한다.

의미 있는 Conflict 또는 Dependency 문제를 해결한 뒤에는 영향을 받은 기능을 반드시 다시 Test한다.

모든 Merge Blocking Condition이 해소된 경우에만 Merge를 진행한다.



6. Overall Development Workflow

1. Target Work Unit 및 Internal Deadline 설정

개발을 시작하기 전에 Dev Team이 다음 개발 단계에 필요한 기술적인 작업을 확인하고, 다음 Upstream PR에 포함할 수 있는 적절한 작업 범위를 제안한다.

Team Leader와 Coordinator Kwanwoo가 함께 Target Work Unit을 최종 Confirm한다.

Target Work Unit을 결정할 때 다음 사항을 고려한다.


* 현재 프로젝트의 우선순위
* 기술적으로 구현 가능한 범위
* 다른 팀과의 Dependency
* Testing 가능 여부
* 예상 개발 일정

Target Work Unit은 하나의 작업일 수도 있고 서로 관련된 여러 작업의 묶음일 수도 있다.

하나의 개발 주기 안에서 완료하고 Test할 수 있으면서, 프로젝트에 의미 있는 변경을 제공할 수 있는 크기로 정한다.

Target Work Unit을 확정할 때 Internal Deadline도 함께 설정한다.

Internal Deadline의 목적은 개발이 불필요하게 계속 늘어지는 것을 방지하고, 팀이 PR 준비 시점을 예측할 수 있도록 하는 것이다.

Deadline에는 다음 과정에 필요한 시간을 고려한다.


* 개발
* Code Review
* Integration
* Functional Testing

예상하지 못한 Dependency 또는 기술적인 문제로 작업이 막힌 경우, Dev Team이 이를 공유하고 Team Leader와 Coordinator가 논의하여 Target Work Unit의 범위를 조정하거나 해당 작업을 다음 Work Unit으로 이동할 수 있다.

완료되지 않은 작업 하나 때문에 전체 PR을 무기한 지연시키지 않는다.


2. Dependency 확인

각 작업을 시작하기 전에 다음 항목과 Dependency 또는 작업 범위 중복이 있는지 확인한다.


* 우리 팀의 다른 Feature
* 공용 코드 또는 Interface
* 다른 팀의 Feature
* 다른 팀에서 관리하는 데이터 또는 로직

Dependency가 불명확한 경우에는 작업 범위가 겹치는 변경을 시작하기 전에 관련 팀과 먼저 소통한다.

현재 프로젝트에서 확인 중인 예시는 다음과 같다.


* Frenchies (Main Menu)  

  Game Over / Final Score 표시 영역의 담당 범위 확인


* Chinese can fly (Records & Achievements System)  

  Gameplay Score와 Final Score의 계산 및 처리 범위 확인


3. 역할 배정

Target Work Unit이 정해지면 각 Task에 Developer와 Reviewer를 지정한다.

Eom Jeong-in은 지정 Tester로서 대부분의 Functional Testing을 담당한다.


4. Feature Branch 생성 및 개발

Developer는 최신 main을 기준으로 해당 작업을 위한 Feature Branch를 생성한다.

이후 다음 과정을 따른다.


1. 할당된 작업 개발
2. 의미 있는 개발 단계마다 Commit
3. 합의한 Commit Message Format 사용
4. 필요한 경우 최신 main의 변경사항 반영
5. 작업 완료 후 Internal PR 생성

5. Review, Test 및 Integration

Reviewer가 Internal PR을 Review한다.

수정이 필요한 경우 Developer는 같은 Feature Branch를 수정한 뒤 다시 Review를 받는다.

Review가 승인되면 Eom Jeong-in이 기능을 Test한다.

Testing까지 통과하면 Seungwoo가 해당 Feature Branch를 우리 팀 main에 Merge한다.

Merge 후 Feature Branch를 삭제한다.

Target Work Unit에 포함된 작업들이 모두 처리될 때까지 이 과정을 반복한다.


6. Upstream PR 준비 및 앞 팀 PR 처리

Target Work Unit이 완료되고 필요한 Internal Review, Integration, Testing까지 끝나면 Team Leaders Chat에 우리 팀 PR이 준비되었다고 알린다.

이후 앞 팀의 Upstream PR을 다음 순서로 처리한다.


1. Seungwoo가 바로 앞 팀의 Upstream PR을 Code Review한다.
2. Seungwoo가 Review를 수행하기 어려운 경우 Kwanwoo가 Backup Reviewer를 담당한다.
3. Reviewer는 변경사항, Dependency, Conflict 가능성 및 Merge Blocking Condition을 확인한다.
4. 문제가 발견되면 필요한 내용을 관련 팀과 소통한다.
5. Review 결과 문제가 없고 Merge Blocking Condition이 없으면 Kwanwoo가 앞 팀 PR을 upstream/main에 Merge한다.

7. 최신 Upstream 반영 및 Final Validation

앞 팀 PR이 upstream/main에 Merge되면 다음 과정을 수행한다.


1. Seungwoo가 변경된 upstream/main을 우리 팀 main에 동기화한다.
2. 필요한 최신 변경사항을 우리 작업에 반영한다.
3. Conflict가 발생하면 Section 5의 규칙에 따라 해결한다.
4. 다른 팀 변경사항으로 인해 영향을 받은 기능을 다시 Test한다.
5. Merge Blocking Condition이 모두 해소되었는지 확인한다.
6. Final Testing을 수행한다.

8. 우리 팀 Upstream PR 생성

Final Validation까지 모두 완료되면 Coordinator Kwanwoo가 우리 팀의 PR을 upstream/main으로 생성한다.

PR 생성 후 Team Leaders Chat에 우리 팀 PR이 생성되었음을 알린다.

이후 우리 팀의 Upstream PR에 대한 Review와 Merge는 다음 PR 준비 팀이 담당한다.



Workflow Diagram

``mermaid
flowchart TD
    A[Dev Team이 작업 범위 제안]
    --> B[Team Leader + Kwanwoo가<br/>Target Work Unit 및 Deadline Confirm]

    B --> C[Dependency 확인]

    C --> D[Developer 및 Reviewer 지정]

    D --> E[최신 main에서<br/>Feature Branch 생성]

    E --> F[개발 및 Commit]

    F --> G[Internal Pull Request]

    G --> H[Code Review]

    H -->|수정 요청| F
    H -->|승인| I[Eom Jeong-in<br/>Functional Testing]

    I -->|실패| F
    I -->|통과| J[Seungwoo가 Team main에 Merge]

    J --> K[Feature Branch 삭제]

    K --> L{Target Work Unit 완료?}

    L -->|아니오| D
    L -->|예| M[우리 팀 PR 준비 완료 선언]

    M --> N[Seungwoo가<br/>앞 팀 Upstream PR Review]

    N -->|Review 불가| N2[Kwanwoo가<br/>Backup Review]
    N -->|Review 완료| O{Merge Blocking<br/>Condition 있음?}
    N2 --> O

    O -->|예| O2[관련 문제 해결 및 재검증]
    O2 --> N

    O -->|아니오| P[Kwanwoo가 앞 팀 PR을<br/>upstream/main에 Merge]

    P --> Q[upstream/main 변경]

    Q --> R[Seungwoo가 Team main 동기화]

    R --> S{Conflict 또는<br/>Dependency 문제 발생?}

    S -->|예| T[Conflict / Dependency 규칙에 따라 해결]
    S -->|아니오| U[Final Testing]

    T --> U

    U -->|실패| V[문제 수정]
    V --> U

    U -->|통과| W[Kwanwoo가 우리 팀 PR을<br/>upstream/main으로 생성]

    W --> X[Team Leaders Chat에 PR 알림]

    X --> Y[다음 PR 준비 팀이<br/>우리 팀 PR Review 및 Merge]``Gameplay HUD Team Git Workflow

1. 선택한 Git Workflow 및 선택 이유

우리 팀은 Feature Branch Workflow를 사용한다.

기존에는 각 팀원이 개인 브랜치를 가지고 있었지만, 개인 브랜치가 실제 작업 단위와 명확하게 연결되지 않았고 일부 팀원은 Git을 적극적으로 사용하지 않아 불필요한 복잡성이 발생했다.

따라서 앞으로는 사람을 기준으로 브랜치를 유지하는 대신, 구체적인 작업 또는 기능이 생길 때마다 해당 작업을 위한 Feature Branch를 생성하는 방식으로 전환한다.

Feature Branch Workflow를 선택한 이유는 다음과 같다.


* 여러 작업을 동시에 병렬적으로 진행할 수 있다.
* 브랜치 이름만으로 어떤 작업을 위한 브랜치인지 확인하기 쉽다.
* main을 안정적인 통합 및 기준 브랜치로 유지할 수 있다.
* 기능별 변경사항을 독립적으로 Review하고 Test할 수 있다.
* 장기간 유지되는 개인 브랜치를 줄일 수 있다.
* 다른 팀과의 Dependency 및 작업 범위 중복을 관리하기 쉽다.



2. Branch Strategy

우리 팀은 기본적으로 다음 두 종류의 브랜치를 사용한다.


* main
* feature/


### main

main은 우리 팀의 통합 및 기준 브랜치이다.

다른 팀의 PR이 upstream/main에 Merge되어 변경사항이 발생하면, Integration Manager가 우리 팀의 main을 최신 upstream/main과 동기화한다.

main에서는 직접 개발하지 않는다.

기능 개발, 버그 수정, 문서 수정 등의 모든 작업은 Feature Branch에서 진행한 뒤 Review와 Testing을 거쳐 main에 통합한다.

### feature/

Feature Branch는 하나의 구체적인 작업 또는 목적을 위해 생성한다.

브랜치 이름은 작업을 수행하는 사람의 이름이 아니라 작업 내용을 기준으로 정한다.

예시:


* feature/game-over-score
* feature/high-score-layout
* feature/score-display
* feature/update-git-workflow

Feature Branch는 실제 작업을 시작할 때 최신 main을 기준으로 생성한다.

작업이 완료되고 Review, Testing, Merge까지 정상적으로 끝나면 해당 Feature Branch는 삭제한다.

기본적인 브랜치 생명주기는 다음과 같다.

latest main → feature branch 생성 → 개발 → review/test → main에 merge → feature branch 삭제



3. Commit Rules

하나의 Feature Branch는 하나의 구체적인 작업 또는 목적을 나타낸다.

하나의 브랜치 안에는 여러 Commit이 존재할 수 있다.

각 Commit에는 하나의 논리적인 변경사항만 포함한다.

서로 관련이 없는 변경사항은 하나의 Commit에 함께 넣지 않는다.

하나의 논리적인 작업을 위해 여러 파일을 수정해야 하는 경우에는 여러 파일의 변경사항이 같은 Commit에 포함될 수 있다.

예를 들어 feature/game-over-score 브랜치에는 다음과 같은 Commit들이 포함될 수 있다.


* feat: add final score display
* fix: correct score update logic
* test: add game over score test

작은 작업이라면 하나의 Commit만 존재할 수도 있다.

예:


* Branch: feature/update-git-workflow
* Commit: docs: update Git workflow

Commit은 아주 작은 수정마다 무분별하게 생성하지 않고, 의미 있는 개발 단계가 완료되었을 때 생성한다.


Commit Message Format

Commit Message는 다음 형식을 따른다.

<type>: <short description>

사용할 Type은 다음과 같다.


Type
	의미

feat
	새로운 기능 추가

fix
	버그 수정

docs
	문서 수정

refactor
	기능 변화 없이 코드 구조 개선

test
	테스트 관련 변경

chore
	설정 또는 유지보수 작업



예시:


* feat: add final score display
* fix: correct game over score calculation
* docs: update Git workflow
* refactor: simplify HUD score handling

update, final, fix, work처럼 변경 목적을 알기 어려운 모호한 Commit Message는 사용하지 않는다.



4. Pull Request and Code Review Rules

Internal Pull Request

모든 개발 변경사항은 Pull Request를 통해 우리 팀의 main에 통합한다.

Developer는 다음 조건을 만족했을 때 Feature Branch에서 main으로 Internal PR을 생성한다.


* 할당된 작업이 완료되었을 것
* Developer가 기본적인 동작 확인을 완료했을 것
* 알려진 Dependency를 확인했을 것
* 해당 브랜치가 Integration 가능한 상태일 것

Developer와 Reviewer

작업을 배정할 때 Developer와 Reviewer를 함께 지정한다.

Reviewer는 Developer 본인이 아닌 다른 팀원이어야 하며, 가능하면 해당 기능이나 코드 영역을 이해하고 있는 개발팀원이 담당한다.

Reviewer는 다음 사항을 확인한다.


* 변경사항이 해당 작업의 목적과 일치하는지
* 관련 없는 변경사항이 포함되지 않았는지
* 명백한 기술적 문제가 없는지
* 다른 기능에 부정적인 영향을 줄 가능성이 있는지
* 알려진 Dependency가 적절하게 처리되었는지

Internal PR을 Merge하기 위해서는 최소 1명의 Reviewer Approval이 필요하다.

수정 요청이 있을 경우 Developer는 동일한 Feature Branch에서 수정한 뒤 다시 Review를 받는다.


Tester

Eom Jeong-in이 우리 팀의 지정 Tester를 담당한다.

Tester는 대부분의 기능 테스트를 담당하며 다음 사항을 확인한다.


* 구현된 기능이 의도대로 동작하는지
* 기존 Gameplay 또는 HUD 기능에 문제가 발생하지 않았는지
* 보고된 버그가 정상적으로 수정되었는지
* 해당 기능이 Integration 가능한 상태인지

Tester, Reviewer 또는 다른 담당자가 사정상 역할을 수행할 수 없는 경우에는 해당 역할을 수행할 수 있는 적절한 팀원이 자율적으로 대신할 수 있다.


Integration Manager

Seungwoo가 우리 팀의 Integration Manager를 담당한다.

Integration Manager의 역할은 다음과 같다.


* Review와 Testing이 완료된 Internal PR의 최종 Merge
* 우리 팀 main으로의 Integration 관리
* upstream/main과 우리 팀 main의 동기화
* 여러 변경사항이 같은 영역에 영향을 줄 경우 Integration 과정 조정

다른 팀의 PR이 실제로 upstream/main에 Merge될 때마다 Seungwoo가 우리 팀의 main을 최신 Upstream 상태로 동기화한다.

Seungwoo가 역할을 수행할 수 없는 경우에는 Integration을 처리할 수 있는 다른 팀원이 대신할 수 있다.


Upstream Pull Request

우리 팀은 사전에 정한 Target Work Unit이 완료되고, 필요한 Internal Integration, Review, Testing까지 모두 완료된 상태를 Upstream PR 준비 완료 상태로 본다.

Upstream PR 과정은 앞 팀의 Upstream PR을 처리하는 과정과 우리 팀의 Upstream PR을 제출하는 과정으로 구분한다.

Previous Team Upstream PR Review and Merge
우리 팀의 Upstream PR이 준비되면 먼저 Team Leaders Chat에 준비 완료 사실을 알린다.

이후 바로 앞 팀의 Upstream PR을 다음 역할 분담에 따라 처리한다.


* Seungwoo가 앞 팀 Upstream PR의 Code Review를 담당한다.
* Seungwoo가 Review를 수행하기 어려운 경우 Coordinator Kwanwoo가 Backup Reviewer를 담당한다.
* Reviewer는 앞 팀의 변경사항, Conflict 가능성, 우리 팀 작업과의 Dependency 등을 확인한다.
* Code Review 결과 문제가 없고 Merge Blocking Condition이 존재하지 않는 경우 Kwanwoo가 앞 팀 PR을 upstream/main에 Merge한다.

앞 팀 PR이 Merge된 이후에는 변경된 upstream/main을 우리 팀의 main에 동기화한다.

이 과정에서 Conflict 또는 기능 영향이 발생하면 Section 5의 규칙에 따라 해결하고 영향을 받은 기능을 다시 Test한다.

Our Team Upstream PR Submission
앞 팀 PR의 Merge 및 최신 upstream/main 동기화가 완료되면 우리 팀의 변경사항에 대한 Final Testing을 수행한다.

Conflict 해결과 Final Testing까지 모두 완료되어 Upstream PR을 제출할 수 있는 상태가 되면 Coordinator Kwanwoo가 우리 팀의 PR을 upstream/main으로 생성한다.

우리 팀의 최종 Upstream PR은 Kwanwoo만 생성한다.

PR 생성 후 Team Leaders Chat에 우리 팀 PR이 생성되었음을 알린다.

이후 우리 팀의 Upstream PR에 대한 Review와 Merge는 다음 PR 준비 팀이 담당한다.

전체 Upstream PR 흐름은 다음과 같다.

우리 팀 PR 준비 완료 → Seungwoo가 앞 팀 PR Review → 필요 시 Kwanwoo가 Backup Review → Kwanwoo가 앞 팀 PR Merge → upstream/main 동기화 → Conflict 해결 및 Final Testing → Kwanwoo가 우리 팀 Upstream PR 생성 → 다음 준비 팀이 우리 팀 PR Review/Merge


Direct Push to main

개발 변경사항을 main에 직접 Push하는 것은 허용하지 않는다.

기능 개발, 버그 수정, 문서 수정은 모두 Feature Branch에서 진행한 뒤 PR을 통해 통합한다.

단, 최신 upstream/main과 우리 팀 main을 동기화하기 위한 작업은 예외이며 Integration Manager가 담당한다.



5. Merge Strategy

우리 팀은 일반 Merge를 기본 Integration 방식으로 사용한다.

일반적인 Workflow에서는 Squash Merge 또는 Rebase를 사용하지 않는다.

이를 통해 Feature 개발 과정에서 생성된 개별 Commit 기록을 유지하고, Shared Commit History를 다시 작성하는 상황을 방지하며, 모든 팀원이 동일한 방식으로 작업할 수 있도록 한다.


Feature Branch → main

Internal PR이 Review와 Testing을 통과하면 Seungwoo가 일반 Merge 방식으로 Feature Branch를 main에 Merge한다.

Merge가 정상적으로 완료되면 해당 Feature Branch는 삭제한다.


최신 main을 Feature Branch에 반영하는 경우

Feature Branch에서 최신 main의 변경사항이 필요한 경우 Rebase 대신 Merge를 사용한다.

Feature Branch는 main이 변경될 때마다 즉시 최신화할 필요는 없다.

다음 상황에서는 최신 main을 Feature Branch에 반영한다.


* 최신 변경사항이 해당 Feature에 영향을 주는 경우
* Internal Integration 전에 필요한 경우
* 우리 팀의 Upstream PR을 준비하는 경우

Merge Conflict Resolution

Conflict의 종류에 따라 해결 담당자를 다르게 한다.

Feature Branch와 main 사이의 Conflict
Feature Branch와 최신 main 사이에서 Conflict가 발생하면 해당 Feature를 담당한 Developer가 우선 해결한다.

Conflict 해결 후에는 변경 결과를 다시 Review하고 Test한다.

우리 팀 내부 Feature 간 Conflict
우리 팀의 두 Feature가 서로 Conflict를 일으키는 경우에는 각 Feature를 담당한 Developer들이 함께 해결한다.

한 Developer가 다른 Developer의 의도한 동작을 임의로 제거하거나 변경하지 않는다.

필요한 경우 Integration Manager가 해결 과정을 조정한다.

다른 팀 변경사항과의 Conflict
다른 팀에서 개발한 의미 있는 기능 또는 코드와 Conflict가 발생한 경우에는 우리 팀에서 임의로 어느 변경사항을 선택하지 않는다.

다음 과정을 따른다.

Conflict 확인 → 관련 팀과 소통 → 의도한 동작 및 Dependency 확인 → 해결 방법 합의 → Conflict 해결 → 재테스트

다른 팀의 의미 있는 변경사항을 임의로 덮어쓰지 않는다.

Formatting 또는 Import 순서처럼 프로그램 동작에 영향을 주지 않는 단순 Conflict는 담당 Developer가 직접 해결할 수 있다.


Merge Blocking Conditions

다음 상황에서는 Merge를 진행하지 않는다.


* Reviewer Approval이 없는 경우
* Merge Conflict가 해결되지 않은 경우
* 필수 Test가 실패한 경우
* 확인되지 않은 Dependency가 있는 경우

Reviewer가 수정이 필요하다고 판단한 경우 해당 변경사항을 먼저 수정하고 다시 Review를 받아야 한다.

Merge Conflict가 발생한 경우 위의 Conflict Resolution 규칙에 따라 해결해야 한다.

필수 Test가 실패한 경우 문제를 수정하고 Test를 다시 수행해야 한다.

다른 Feature, 공용 코드 또는 다른 팀 작업과의 Dependency가 확인되지 않은 경우에는 관련 담당자 또는 팀과 먼저 소통하여 Dependency와 작업 범위를 명확히 해야 한다.

의미 있는 Conflict 또는 Dependency 문제를 해결한 뒤에는 영향을 받은 기능을 반드시 다시 Test한다.

모든 Merge Blocking Condition이 해소된 경우에만 Merge를 진행한다.



6. Overall Development Workflow

1. Target Work Unit 및 Internal Deadline 설정

개발을 시작하기 전에 Dev Team이 다음 개발 단계에 필요한 기술적인 작업을 확인하고, 다음 Upstream PR에 포함할 수 있는 적절한 작업 범위를 제안한다.

Team Leader와 Coordinator Kwanwoo가 함께 Target Work Unit을 최종 Confirm한다.

Target Work Unit을 결정할 때 다음 사항을 고려한다.


* 현재 프로젝트의 우선순위
* 기술적으로 구현 가능한 범위
* 다른 팀과의 Dependency
* Testing 가능 여부
* 예상 개발 일정

Target Work Unit은 하나의 작업일 수도 있고 서로 관련된 여러 작업의 묶음일 수도 있다.

하나의 개발 주기 안에서 완료하고 Test할 수 있으면서, 프로젝트에 의미 있는 변경을 제공할 수 있는 크기로 정한다.

Target Work Unit을 확정할 때 Internal Deadline도 함께 설정한다.

Internal Deadline의 목적은 개발이 불필요하게 계속 늘어지는 것을 방지하고, 팀이 PR 준비 시점을 예측할 수 있도록 하는 것이다.

Deadline에는 다음 과정에 필요한 시간을 고려한다.


* 개발
* Code Review
* Integration
* Functional Testing

예상하지 못한 Dependency 또는 기술적인 문제로 작업이 막힌 경우, Dev Team이 이를 공유하고 Team Leader와 Coordinator가 논의하여 Target Work Unit의 범위를 조정하거나 해당 작업을 다음 Work Unit으로 이동할 수 있다.

완료되지 않은 작업 하나 때문에 전체 PR을 무기한 지연시키지 않는다.


2. Dependency 확인

각 작업을 시작하기 전에 다음 항목과 Dependency 또는 작업 범위 중복이 있는지 확인한다.


* 우리 팀의 다른 Feature
* 공용 코드 또는 Interface
* 다른 팀의 Feature
* 다른 팀에서 관리하는 데이터 또는 로직

Dependency가 불명확한 경우에는 작업 범위가 겹치는 변경을 시작하기 전에 관련 팀과 먼저 소통한다.

현재 프로젝트에서 확인 중인 예시는 다음과 같다.


* Frenchies (Main Menu)  

  Game Over / Final Score 표시 영역의 담당 범위 확인


* Chinese can fly (Records & Achievements System)  

  Gameplay Score와 Final Score의 계산 및 처리 범위 확인


3. 역할 배정

Target Work Unit이 정해지면 각 Task에 Developer와 Reviewer를 지정한다.

Eom Jeong-in은 지정 Tester로서 대부분의 Functional Testing을 담당한다.


4. Feature Branch 생성 및 개발

Developer는 최신 main을 기준으로 해당 작업을 위한 Feature Branch를 생성한다.

이후 다음 과정을 따른다.


1. 할당된 작업 개발
2. 의미 있는 개발 단계마다 Commit
3. 합의한 Commit Message Format 사용
4. 필요한 경우 최신 main의 변경사항 반영
5. 작업 완료 후 Internal PR 생성

5. Review, Test 및 Integration

Reviewer가 Internal PR을 Review한다.

수정이 필요한 경우 Developer는 같은 Feature Branch를 수정한 뒤 다시 Review를 받는다.

Review가 승인되면 Eom Jeong-in이 기능을 Test한다.

Testing까지 통과하면 Seungwoo가 해당 Feature Branch를 우리 팀 main에 Merge한다.

Merge 후 Feature Branch를 삭제한다.

Target Work Unit에 포함된 작업들이 모두 처리될 때까지 이 과정을 반복한다.


6. Upstream PR 준비 및 앞 팀 PR 처리

Target Work Unit이 완료되고 필요한 Internal Review, Integration, Testing까지 끝나면 Team Leaders Chat에 우리 팀 PR이 준비되었다고 알린다.

이후 앞 팀의 Upstream PR을 다음 순서로 처리한다.


1. Seungwoo가 바로 앞 팀의 Upstream PR을 Code Review한다.
2. Seungwoo가 Review를 수행하기 어려운 경우 Kwanwoo가 Backup Reviewer를 담당한다.
3. Reviewer는 변경사항, Dependency, Conflict 가능성 및 Merge Blocking Condition을 확인한다.
4. 문제가 발견되면 필요한 내용을 관련 팀과 소통한다.
5. Review 결과 문제가 없고 Merge Blocking Condition이 없으면 Kwanwoo가 앞 팀 PR을 upstream/main에 Merge한다.

7. 최신 Upstream 반영 및 Final Validation

앞 팀 PR이 upstream/main에 Merge되면 다음 과정을 수행한다.


1. Seungwoo가 변경된 upstream/main을 우리 팀 main에 동기화한다.
2. 필요한 최신 변경사항을 우리 작업에 반영한다.
3. Conflict가 발생하면 Section 5의 규칙에 따라 해결한다.
4. 다른 팀 변경사항으로 인해 영향을 받은 기능을 다시 Test한다.
5. Merge Blocking Condition이 모두 해소되었는지 확인한다.
6. Final Testing을 수행한다.

8. 우리 팀 Upstream PR 생성

Final Validation까지 모두 완료되면 Coordinator Kwanwoo가 우리 팀의 PR을 upstream/main으로 생성한다.

PR 생성 후 Team Leaders Chat에 우리 팀 PR이 생성되었음을 알린다.

이후 우리 팀의 Upstream PR에 대한 Review와 Merge는 다음 PR 준비 팀이 담당한다.



Workflow Diagram

``mermaid
flowchart TD
    A[Dev Team이 작업 범위 제안]
    --> B[Team Leader + Kwanwoo가<br/>Target Work Unit 및 Deadline Confirm]

    B --> C[Dependency 확인]

    C --> D[Developer 및 Reviewer 지정]

    D --> E[최신 main에서<br/>Feature Branch 생성]

    E --> F[개발 및 Commit]

    F --> G[Internal Pull Request]

    G --> H[Code Review]

    H -->|수정 요청| F
    H -->|승인| I[Eom Jeong-in<br/>Functional Testing]

    I -->|실패| F
    I -->|통과| J[Seungwoo가 Team main에 Merge]

    J --> K[Feature Branch 삭제]

    K --> L{Target Work Unit 완료?}

    L -->|아니오| D
    L -->|예| M[우리 팀 PR 준비 완료 선언]

    M --> N[Seungwoo가<br/>앞 팀 Upstream PR Review]

    N -->|Review 불가| N2[Kwanwoo가<br/>Backup Review]
    N -->|Review 완료| O{Merge Blocking<br/>Condition 있음?}
    N2 --> O

    O -->|예| O2[관련 문제 해결 및 재검증]
    O2 --> N

    O -->|아니오| P[Kwanwoo가 앞 팀 PR을<br/>upstream/main에 Merge]

    P --> Q[upstream/main 변경]

    Q --> R[Seungwoo가 Team main 동기화]

    R --> S{Conflict 또는<br/>Dependency 문제 발생?}

    S -->|예| T[Conflict / Dependency 규칙에 따라 해결]
    S -->|아니오| U[Final Testing]

    T --> U

    U -->|실패| V[문제 수정]
    V --> U

    U -->|통과| W[Kwanwoo가 우리 팀 PR을<br/>upstream/main으로 생성]

    W --> X[Team Leaders Chat에 PR 알림]

    X --> Y[다음 PR 준비 팀이<br/>우리 팀 PR Review 및 Merge]`
