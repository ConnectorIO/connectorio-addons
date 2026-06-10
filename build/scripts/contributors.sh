#/bin/env sh

# Find files with external contributions
# Counting contributors: END { for (f in count) print count[f], f }
# Showing just files: END { for (f in count) print f }
git log --name-only --format="AUTHOR:%ae" -- '*' | awk '
  /^AUTHOR:/ {
    author=substr($0, 8)
    if (author == "luke@code-house.org" || author ~ /@connectorio\.com$/)
      author=""
    next
  }
  /^$/  { next }
  author=="" { next }
  {
    key = author SUBSEP $0
    if (!(key in seen)) {
      seen[key] = 1
      count[$0]++
    }
  }
  END { for (f in count) print f }
' | sort -rn | head -30
