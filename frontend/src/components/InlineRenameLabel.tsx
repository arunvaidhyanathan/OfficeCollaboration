import { useState, type FocusEvent, type KeyboardEvent } from 'react';

interface InlineRenameLabelProps {
  name: string;
  onRename: (newName: string) => void;
  className?: string;
}

/** Double-click to rename in place; Enter/blur commits, Escape cancels. */
export default function InlineRenameLabel({ name, onRename, className }: InlineRenameLabelProps) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(name);

  function startEditing(e: React.MouseEvent) {
    e.stopPropagation();
    setDraft(name);
    setEditing(true);
  }

  function selectAll(e: FocusEvent<HTMLInputElement>) {
    e.target.select();
  }

  function commit() {
    setEditing(false);
    const trimmed = draft.trim();
    if (trimmed && trimmed !== name) {
      onRename(trimmed);
    }
  }

  function onKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter') {
      e.currentTarget.blur();
    } else if (e.key === 'Escape') {
      setDraft(name);
      setEditing(false);
    }
  }

  if (editing) {
    return (
      <input
        autoFocus
        onFocus={selectAll}
        value={draft}
        onChange={(e) => setDraft(e.target.value)}
        onBlur={commit}
        onKeyDown={onKeyDown}
        onClick={(e) => e.stopPropagation()}
        className={`border border-blue-400 rounded px-1 -mx-1 outline-none ${className ?? ''}`}
      />
    );
  }

  return (
    <span
      onClick={(e) => e.stopPropagation()}
      onDoubleClick={startEditing}
      title="Double-click to rename"
      className={className}
    >
      {name}
    </span>
  );
}
