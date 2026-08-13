interface Props {
  status: string;
}

export function StatusBadge({ status }: Props) {
  const cls = `badge badge-${status.toLowerCase()}`;
  return <span className={cls}>{status}</span>;
}
