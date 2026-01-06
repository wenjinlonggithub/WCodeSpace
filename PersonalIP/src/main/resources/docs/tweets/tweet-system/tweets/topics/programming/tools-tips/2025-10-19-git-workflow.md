# Git工作流：从混乱到有序

## 推文内容

以前的Git记录：
- "update"
- "fix bug"
- "修改"
- "aaa"

现在回看：完全不知道改了啥 😭

**现在的Commit Message：**

```
feat(auth): implement JWT authentication

- Add JWT token generation
- Create auth middleware
- Add refresh token logic
- Update user model with tokenVersion

Closes #123
```

清晰明了

**我的Git Workflow：**

**1. Commit Message规范**

格式：
```
<type>(<scope>): <subject>

<body>

<footer>
```

Type:
- feat: 新功能
- fix: Bug修复
- docs: 文档
- refactor: 重构
- test: 测试
- chore: 构建/工具

**2. Feature Branch策略**

```
main (生产)
  ├─ develop (开发)
      ├─ feature/user-auth
      ├─ feature/payment
      └─ hotfix/login-bug
```

每个功能独立分支

**3. Commit频率**

❌ 一天结束一个大commit
✅ 每个逻辑单元一个commit

好处：
- 容易rollback
- Code Review清晰
- 历史可追踪

**4. 有用的Git命令**

```bash
# 修改最后一次commit
git commit --amend

# 交互式rebase
git rebase -i HEAD~3

# 临时保存
git stash
git stash pop

# 查看某个文件的修改历史
git log -p filename

# 找到引入bug的commit
git bisect
```

**5. .gitignore必须有**

别把这些提交：
- node_modules/
- .env
- *.log
- .DS_Store
- ide配置

**6. PR Title规范**

```
[FEAT] Implement user authentication

Changes:
- JWT token system
- Login/Logout endpoints
- Auth middleware

Test Plan:
- Manual testing on dev
- Unit tests added
- Integration tests passed
```

**7. Code Review前**

Checklist:
□ 自己先review一遍
□ 运行所有测试
□ 更新文档
□ Rebase到最新main
□ 解决conflicts

**8. 保护分支**

main分支设置：
- 禁止直接push
- 必须PR
- 必须review通过
- CI/CD必须过

**9. Tag版本**

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

语义化版本：
- v1.0.0 (主版本.次版本.补丁)

**10. 定期清理**

```bash
# 删除已合并的本地分支
git branch --merged | grep -v "\*" | xargs -n 1 git branch -d

# 清理远程已删除的分支
git fetch --prune
```

**救命技巧：**

搞砸了？

```bash
# 回到任何时刻
git reflog
git reset --hard <commit-hash>

# 找回删除的commit
git reflog找到hash
git cherry-pick <hash>
```

Git = 时光机

**团队协作：**

约定：
1. Commit message统一格式
2. 分支命名规范
3. PR模板
4. Review标准

混乱→有序

你的Git工作流是什么？

---

## 标签
#Git #版本控制 #开发流程

## 发布建议
- 分享Git graph截图
- 附workflow图解
