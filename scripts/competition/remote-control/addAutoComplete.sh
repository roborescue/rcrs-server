#!/bin/bash
export PATH=$PATH:$HOME/scripts/remote-control:$HOME/scripts/evaluation




_run_sh_autocomplete() {
  . ~/scripts/remote-control/config.sh
  local cur prev words cword
  _get_comp_words_by_ref -n =: cur prev words cword
  case $cword in
    1)
      COMPREPLY=( $(compgen -W "$CLUSTERS" -- "$cur") )
      ;;
    2)
        COMPREPLY=( $(compgen -W "$(ls "$HOME/$MAPDIR")" -- "$cur") )
      ;;
    3)
      COMPREPLY=( $(compgen -W "$TEAM_SHORTHANDS" -- "$cur") )
      ;;
  esac
}

complete -F _run_sh_autocomplete run.sh




_rcrsync_autocomplete() {
  . ~/scripts/remote-control/config.sh
  local cur prev cword
  COMPREPLY=()
  cur="${COMP_WORDS[COMP_CWORD]}"
  prev="${COMP_WORDS[COMP_CWORD-1]}"
  cword=$COMP_CWORD

  local SUBCOMMANDS="all scripts kernel maps code logs"
  local CODEDIR="${CODEDIR:-$HOME/code}"  # default to $HOME/code if not set

  if [[ $cword -eq 1 ]]; then
    # Suggest subcommands
    COMPREPLY=( $(compgen -W "$SUBCOMMANDS" -- "$cur") )
    return
  fi

  if [[ ${COMP_WORDS[1]} == "code" && $cword -eq 2 ]]; then
    # Suggest folders inside $CODEDIR
      COMPREPLY=( $(compgen -W "$TEAM_SHORTHANDS" -- "$cur") )
    return
  fi
}

complete -F _rcrsync_autocomplete rcrsync



_evalLog_autocomplete() {
  . ~/scripts/remote-control/config.sh
  local cur prev cword
  COMPREPLY=()
  cur="${COMP_WORDS[COMP_CWORD]}"
  prev="${COMP_WORDS[COMP_CWORD-1]}"
  cword=$COMP_CWORD

  local BASE="$HOME/$LOGDIR/$DAY/kernel"

  if [[ $cword -eq 1  ]]; then
    # Suggest files under kernel/
    if [[ -d "$BASE" ]]; then
      local files=$(find "$BASE" -type f)
      COMPREPLY=( $(compgen -W "$files" -- "$cur") )
    fi
  fi
}

complete -F _evalLog_autocomplete evalLog.sh