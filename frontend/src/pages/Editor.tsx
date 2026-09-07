import { useEffect, useRef, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { DocumentEditor } from '@onlyoffice/document-editor-react';
import { ArrowLeft, CloudCheck, MoreHorizontal, Download } from 'lucide-react';
import { getEditorConfig, downloadUrl, type OnlyOfficeConfig } from '../lib/api';

const TOP_BAR_HEIGHT = 48;

export default function Editor() {
  const { id } = useParams<{ id: string }>();
  const [config, setConfig] = useState<OnlyOfficeConfig | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!id) return;
    let active = true;
    setConfig(null);
    setError(null);
    getEditorConfig(id)
      .then((cfg) => {
        if (active) setConfig(cfg);
      })
      .catch((err) => {
        if (active) setError(err instanceof Error ? err.message : 'Failed to load editor config');
      });
    return () => {
      active = false;
    };
  }, [id]);

  useEffect(() => {
    function onClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) setMenuOpen(false);
    }
    document.addEventListener('mousedown', onClickOutside);
    return () => document.removeEventListener('mousedown', onClickOutside);
  }, []);

  const documentName =
    (config && typeof config.document === 'object' && config.document !== null
      ? (config.document as Record<string, unknown>).title
      : undefined) ?? 'Document';

  return (
    <div className="flex h-screen flex-col">
      <header
        style={{ height: TOP_BAR_HEIGHT }}
        className="flex shrink-0 items-center justify-between border-b border-gray-200 px-4"
      >
        <div className="flex min-w-0 items-center gap-3">
          <Link to="/" className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-800">
            <ArrowLeft size={15} />
            Documents
          </Link>
          <span className="truncate text-sm font-semibold text-gray-800">{String(documentName)}</span>
        </div>

        <div className="flex items-center gap-3">
          {config && (
            <span className="flex items-center gap-1 text-xs text-gray-400">
              <CloudCheck size={14} />
              Saved to S3
            </span>
          )}
          <div ref={menuRef} className="relative">
            <button
              type="button"
              onClick={() => setMenuOpen((v) => !v)}
              aria-label="Document actions"
              className="rounded-md p-1.5 text-gray-500 hover:bg-gray-100"
            >
              <MoreHorizontal size={16} />
            </button>
            {menuOpen && id && (
              <div className="absolute right-0 mt-1 w-40 rounded-lg border border-gray-200 bg-white py-1 shadow-lg z-20">
                <a
                  href={downloadUrl(id)}
                  download={String(documentName)}
                  onClick={() => setMenuOpen(false)}
                  className="flex items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
                >
                  <Download size={15} />
                  Download
                </a>
              </div>
            )}
          </div>
        </div>
      </header>

      <div style={{ height: `calc(100vh - ${TOP_BAR_HEIGHT}px)` }}>
        {error && <p className="p-4 text-sm text-red-600">{error}</p>}
        {!error && !config && <p className="p-4 text-sm text-gray-500">Loading editor...</p>}
        {config && (
          <DocumentEditor
            id="onlyoffice-editor"
            documentServerUrl={import.meta.env.VITE_ONLYOFFICE_URL}
            config={config}
            events_onDocumentReady={() => {}}
            events_onError={(e: unknown) => console.error('ONLYOFFICE error', e)}
          />
        )}
      </div>
    </div>
  );
}
