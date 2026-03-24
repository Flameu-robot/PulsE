package com.example.messengerservice.entity.groups;

import com.example.messengerservice.entity.enums.GroupFeatures;
import com.example.messengerservice.entity.enums.GroupType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id")
    private Long ownerId;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupType type;

    @Column(name = "dm_hash_key", length = 50, unique = true)
    private String dmHashKey;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private GroupFeatures features;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupMember> members = new ArrayList<>();;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<TextChannel> channels = new ArrayList<>();;
}